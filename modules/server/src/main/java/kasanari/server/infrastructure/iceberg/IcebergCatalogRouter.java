package kasanari.server.infrastructure.iceberg;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import kasanari.catalog.iceberg.IcebergCatalogAdapter;
import kasanari.management.catalog.ManagementCatalogService;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class IcebergCatalogRouter {
    private static final Logger logger = Logger.getLogger(IcebergCatalogRouter.class);

    // todo: regularly fetch actual catalogs from DB
    private final ManagementCatalogService catalogService;
    private final Map<String, IcebergCatalogAdapter> icebergCatalogs;

    public IcebergCatalogRouter(ManagementCatalogService catalogService) {
        this.catalogService = catalogService;
        this.icebergCatalogs = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void initializeCatalogs() {
        // todo: load and init iceberg catalogs from DB
    }

    public IcebergCatalogAdapter getOrThrow(String name) {
        var maybeCatalog = icebergCatalogs.get(name);
        if (maybeCatalog == null) {
            throw new NotFoundException("Iceberg catalog wasn't found");
        }

        return maybeCatalog;
    }
}
