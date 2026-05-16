package kasanari.server.infrastructure.paimon;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import kasanari.catalog.paimon.PaimonCatalogAdapter;
import kasanari.management.catalog.ManagementCatalogService;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class PaimonCatalogRouter {
    private static final Logger logger = Logger.getLogger(PaimonCatalogRouter.class);

    // todo: regularly fetch actual catalogs from DB
    private final ManagementCatalogService catalogService;
    private final Map<String, PaimonCatalogAdapter> paimonCatalogs;

    public PaimonCatalogRouter(ManagementCatalogService catalogService) {
        this.catalogService = catalogService;
        this.paimonCatalogs = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void initializeCatalogs() {
        // todo: load and init iceberg catalogs from DB
    }

    public PaimonCatalogAdapter getOrThrow(String name) {
        var maybeCatalog = paimonCatalogs.get(name);
        if (maybeCatalog == null) {
            throw new NotFoundException("Paimon catalog wasn't found");
        }

        return maybeCatalog;
    }
}
