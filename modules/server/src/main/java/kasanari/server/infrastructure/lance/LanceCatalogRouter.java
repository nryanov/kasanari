package kasanari.server.infrastructure.lance;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import kasanari.catalog.lance.LanceCatalogAdapter;
import kasanari.management.catalog.ManagementCatalogService;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class LanceCatalogRouter {
    private static final Logger logger = Logger.getLogger(LanceCatalogRouter.class);

    // todo: regularly fetch actual catalogs from DB
    private final ManagementCatalogService catalogService;
    private final Map<String, LanceCatalogAdapter> lanceCatalogs;

    public LanceCatalogRouter(ManagementCatalogService catalogService) {
        this.catalogService = catalogService;
        this.lanceCatalogs = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void initializeCatalogs() {
        // todo: load and init iceberg catalogs from DB
    }

    public LanceCatalogAdapter getOrThrow(String name) {
        var maybeCatalog = lanceCatalogs.get(name);
        if (maybeCatalog == null) {
            throw new NotFoundException("Lance catalog wasn't found");
        }

        return maybeCatalog;
    }
}
