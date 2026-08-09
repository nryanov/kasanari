package kasanari.server.infrastructure.lance;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import kasanari.catalog.lance.KasanariLanceCatalogFactory;
import kasanari.catalog.lance.KasanariLanceProperties;
import kasanari.catalog.lance.LanceCatalogAdapter;
import kasanari.catalog.lance.ProxyLanceCatalogFactory;
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
public class LanceCatalogRouter {
    private static final Logger logger = Logger.getLogger(LanceCatalogRouter.class);
    private static final CatalogType CATALOG_TYPE = CatalogType.LANCE;
    private static final String IMPLEMENTATION_PROPERTY = "implementation";

    private final ManagementCatalogService catalogService;
    private final Map<String, LanceCatalogAdapter> lanceCatalogs;
    private final Map<String, Long> catalogVersions;

    private final KasanariLanceCatalogFactory kasanariLanceCatalogFactory;
    private final ProxyLanceCatalogFactory proxyLanceCatalogFactory;

    private ScheduledExecutorService refreshExecutor;

    @ConfigProperty(name = "kasanari.catalog.refresh-interval", defaultValue = "30s")
    Duration refreshInterval;

    public LanceCatalogRouter(ManagementCatalogService catalogService) {
        this.catalogService = catalogService;
        this.lanceCatalogs = new ConcurrentHashMap<>();
        this.catalogVersions = new ConcurrentHashMap<>();

        this.kasanariLanceCatalogFactory = new KasanariLanceCatalogFactory();
        this.proxyLanceCatalogFactory = new ProxyLanceCatalogFactory();
    }

    @PostConstruct
    public void start() {
        syncFromDatabase();
        refreshExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            var thread = new Thread(r, "lance-catalog-refresh");
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

    public LanceCatalogAdapter getOrThrow(String name) {
        var maybeCatalog = lanceCatalogs.get(name);
        if (maybeCatalog == null) {
            throw new NotFoundException("Lance catalog wasn't found");
        }

        return maybeCatalog;
    }

    private synchronized void syncFromDatabase() {
        List<CatalogMetadata> catalogs;
        try {
            catalogs = catalogService.list(CATALOG_TYPE);
        } catch (Exception e) {
            logger.error("Failed to fetch Lance catalogs from DB", e);
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
            closeQuietly(lanceCatalogs.remove(name));
            catalogVersions.remove(name);
            logger.infof("Replacing Lance catalog '%s' (version %d -> %d)", name, knownVersion, catalog.version());
        } else {
            logger.infof("Adding Lance catalog '%s' (version %d)", name, catalog.version());
        }

        try {
            var adapter = createAdapter(catalog);
            if (adapter == null) {
                return;
            }
            lanceCatalogs.put(name, adapter);
            catalogVersions.put(name, catalog.version());
        } catch (Exception e) {
            logger.errorf(e, "Failed to create Lance catalog '%s'", name);
        }
    }

    private void removeCatalog(String name) {
        closeQuietly(lanceCatalogs.remove(name));
        catalogVersions.remove(name);
        logger.infof("Removed Lance catalog '%s'", name);
    }

    private LanceCatalogAdapter createAdapter(CatalogMetadata catalog) {
        var fileIoConfig = catalog.spec().fileIoProperties();
        var properties = catalog.spec().catalogProperties();
        var implementation = properties.get(IMPLEMENTATION_PROPERTY);
        if (implementation == null || implementation.isBlank()) {
            logger.errorf("Lance catalog '%s' is missing required property '%s'", catalog.catalogName(), IMPLEMENTATION_PROPERTY);
            return null;
        }

        return switch (catalog.catalogMode()) {
            case INTERNAL -> {
                var internalProperties = new HashMap<>(properties);
                // Thread management catalog id for Yugabyte row isolation / hash sharding.
                internalProperties.putIfAbsent(KasanariLanceProperties.CATALOG_KEY, catalog.catalogName());
                yield kasanariLanceCatalogFactory.create(implementation, fileIoConfig, internalProperties);
            }
            case PROXY -> proxyLanceCatalogFactory.create(implementation, fileIoConfig, properties);
        };
    }

    private void closeQuietly(LanceCatalogAdapter adapter) {
        if (adapter == null) {
            return;
        }
        try {
            adapter.close();
        } catch (Exception e) {
            logger.warnf(e, "Failed to close Lance catalog adapter");
        }
    }
}
