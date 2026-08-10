package kasanari.server.infrastructure.paimon;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import kasanari.catalog.paimon.KasanariPaimonCatalogFactory;
import kasanari.catalog.paimon.PaimonCatalogAdapter;
import kasanari.catalog.paimon.ProxyPaimonCatalogFactory;
import kasanari.management.catalog.ManagementCatalogService;
import kasanari.repository.management.catalog.model.CatalogMetadata;
import kasanari.core.model.CatalogType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class PaimonCatalogRouter {
    private static final Logger logger = Logger.getLogger(PaimonCatalogRouter.class);
    private static final CatalogType CATALOG_TYPE = CatalogType.PAIMON;

    private final ManagementCatalogService catalogService;
    private final Map<String, PaimonCatalogAdapter> paimonCatalogs;
    private final Map<String, Long> catalogVersions;

    private final KasanariPaimonCatalogFactory kasanariPaimonCatalogFactory;
    private final ProxyPaimonCatalogFactory proxyPaimonCatalogFactory;

    private ScheduledExecutorService refreshExecutor;

    @ConfigProperty(name = "kasanari.catalog.refresh-interval", defaultValue = "30s")
    Duration refreshInterval;

    public PaimonCatalogRouter(ManagementCatalogService catalogService) {
        this.catalogService = catalogService;
        this.paimonCatalogs = new ConcurrentHashMap<>();
        this.catalogVersions = new ConcurrentHashMap<>();

        this.kasanariPaimonCatalogFactory = new KasanariPaimonCatalogFactory();
        this.proxyPaimonCatalogFactory = new ProxyPaimonCatalogFactory();
    }

    @PostConstruct
    public void start() {
        syncFromDatabase();
        refreshExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            var thread = new Thread(r, "paimon-catalog-refresh");
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

    public PaimonCatalogAdapter getOrThrow(String name) {
        var maybeCatalog = paimonCatalogs.get(name);
        if (maybeCatalog == null) {
            throw new NotFoundException("Paimon catalog wasn't found");
        }

        return maybeCatalog;
    }

    private synchronized void syncFromDatabase() {
        List<CatalogMetadata> catalogs;
        try {
            catalogs = catalogService.list(CATALOG_TYPE);
        } catch (Exception e) {
            logger.error("Failed to fetch Paimon catalogs from DB", e);
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
            paimonCatalogs.remove(name);
            catalogVersions.remove(name);
            logger.infof("Replacing Paimon catalog '%s' (version %d -> %d)", name, knownVersion, catalog.version());
        } else {
            logger.infof("Adding Paimon catalog '%s' (version %d)", name, catalog.version());
        }

        try {
            var adapter = createAdapter(catalog);
            paimonCatalogs.put(name, adapter);
            catalogVersions.put(name, catalog.version());
        } catch (Exception e) {
            logger.errorf(e, "Failed to create Paimon catalog '%s'", name);
        }
    }

    private void removeCatalog(String name) {
        paimonCatalogs.remove(name);
        catalogVersions.remove(name);
        logger.infof("Removed Paimon catalog '%s'", name);
    }

    private PaimonCatalogAdapter createAdapter(CatalogMetadata catalog) {
        return switch (catalog.catalogMode()) {
            case INTERNAL -> kasanariPaimonCatalogFactory.create(
                    catalog.catalogName(),
                    catalog.spec().fileIoProperties(),
                    catalog.spec().catalogProperties()
            );
            case PROXY -> proxyPaimonCatalogFactory.create(
                    catalog.catalogName(),
                    catalog.spec().fileIoProperties(),
                    catalog.spec().catalogProperties()
            );
        };
    }
}
