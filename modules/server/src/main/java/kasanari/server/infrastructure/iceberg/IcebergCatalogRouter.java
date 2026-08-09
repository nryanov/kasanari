package kasanari.server.infrastructure.iceberg;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import kasanari.catalog.iceberg.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.KasanariIcebergCatalogFactory;
import kasanari.catalog.iceberg.KasanariIcebergProperties;
import kasanari.catalog.iceberg.ProxyIcebergCatalogFactory;
import kasanari.management.catalog.ManagementCatalogService;
import kasanari.repository.management.catalog.model.CatalogMetadata;
import kasanari.core.model.CatalogType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class IcebergCatalogRouter {
    private static final Logger logger = Logger.getLogger(IcebergCatalogRouter.class);
    private static final CatalogType CATALOG_TYPE = CatalogType.ICEBERG;

    private final ManagementCatalogService catalogService;
    private final Map<String, IcebergCatalogAdapter> icebergCatalogs;
    private final Map<String, Long> catalogVersions;

    private final KasanariIcebergCatalogFactory kasanariIcebergCatalogFactory;
    private final ProxyIcebergCatalogFactory proxyIcebergCatalogFactory;

    private ScheduledExecutorService refreshExecutor;

    @ConfigProperty(name = "kasanari.catalog.refresh-interval", defaultValue = "30s")
    Duration refreshInterval;

    public IcebergCatalogRouter(ManagementCatalogService catalogService) {
        this.catalogService = catalogService;
        this.icebergCatalogs = new ConcurrentHashMap<>();
        this.catalogVersions = new ConcurrentHashMap<>();

        this.kasanariIcebergCatalogFactory = new KasanariIcebergCatalogFactory();
        this.proxyIcebergCatalogFactory = new ProxyIcebergCatalogFactory();
    }

    @PostConstruct
    public void start() {
        syncFromDatabase();
        refreshExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            var thread = new Thread(r, "iceberg-catalog-refresh");
            thread.setDaemon(true);
            return thread;
        });
        refreshExecutor.scheduleWithFixedDelay(
                this::syncFromDatabase,
                refreshInterval.toSeconds(),
                refreshInterval.toSeconds(),
                TimeUnit.SECONDS
        );
    }

    @PreDestroy
    public void stop() {
        if (refreshExecutor != null) {
            refreshExecutor.shutdown();
        }
    }

    public IcebergCatalogAdapter getOrThrow(String name) {
        var maybeCatalog = icebergCatalogs.get(name);
        if (maybeCatalog == null) {
            throw new NotFoundException("Iceberg catalog wasn't found");
        }

        return maybeCatalog;
    }

    private synchronized void syncFromDatabase() {
        List<CatalogMetadata> catalogs;
        try {
            catalogs = catalogService.list(CATALOG_TYPE);
        } catch (Exception e) {
            logger.error("Failed to fetch Iceberg catalogs from DB", e);
            return;
        }

        var dbNames = new HashSet<String>();
        for (var catalog : catalogs) {
            dbNames.add(catalog.catalogName());
            var knownVersion = catalogVersions.get(catalog.catalogName());
            if (knownVersion == null || knownVersion != catalog.version()) {
                replaceCatalog(catalog);
            }
        }

        for (var name : new ArrayList<>(catalogVersions.keySet())) {
            if (!dbNames.contains(name)) {
                removeCatalog(name);
            }
        }
    }

    private void replaceCatalog(CatalogMetadata catalog) {
        var name = catalog.catalogName();
        var knownVersion = catalogVersions.get(name);
        if (knownVersion != null) {
            icebergCatalogs.remove(name);
            catalogVersions.remove(name);
            logger.infof("Replacing Iceberg catalog '%s' (version %d -> %d)", name, knownVersion, catalog.version());
        } else {
            logger.infof("Adding Iceberg catalog '%s' (version %d)", name, catalog.version());
        }

        try {
            var adapter = createAdapter(catalog);
            icebergCatalogs.put(name, adapter);
            catalogVersions.put(name, catalog.version());
        } catch (Exception e) {
            logger.errorf(e, "Failed to create Iceberg catalog '%s'", name);
        }
    }

    private void removeCatalog(String name) {
        icebergCatalogs.remove(name);
        catalogVersions.remove(name);
        logger.infof("Removed Iceberg catalog '%s'", name);
    }

    private IcebergCatalogAdapter createAdapter(CatalogMetadata catalog) {
        return switch (catalog.catalogMode()) {
            case INTERNAL -> {
                var internalProperties = new HashMap<>(catalog.spec().catalogProperties());
                internalProperties.putIfAbsent(KasanariIcebergProperties.CATALOG_NAME, catalog.catalogName());
                yield kasanariIcebergCatalogFactory.create(
                        catalog.catalogName(),
                        catalog.spec().fileIoProperties(),
                        internalProperties
                );
            }
            case PROXY -> proxyIcebergCatalogFactory.create(
                    catalog.catalogName(),
                    catalog.spec().fileIoProperties(),
                    catalog.spec().catalogProperties()
            );
        };
    }
}
