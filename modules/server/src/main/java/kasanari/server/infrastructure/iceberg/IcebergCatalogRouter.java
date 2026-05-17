package kasanari.server.infrastructure.iceberg;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import kasanari.catalog.iceberg.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.KasanariIcebergCatalogFactory;
import kasanari.catalog.iceberg.ProxyIcebergCatalogFactory;
import kasanari.management.catalog.ManagementCatalogService;
import kasanari.repository.management.common.model.CatalogType;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class IcebergCatalogRouter {
    private static final Logger logger = Logger.getLogger(IcebergCatalogRouter.class);

    // todo: regularly fetch actual catalogs from DB
    private final ManagementCatalogService catalogService;
    private final Map<String, IcebergCatalogAdapter> icebergCatalogs;

    private final KasanariIcebergCatalogFactory kasanariIcebergCatalogFactory;
    private final ProxyIcebergCatalogFactory proxyIcebergCatalogFactory;

    public IcebergCatalogRouter(ManagementCatalogService catalogService) {
        this.catalogService = catalogService;
        this.icebergCatalogs = new ConcurrentHashMap<>();

        this.kasanariIcebergCatalogFactory = new KasanariIcebergCatalogFactory();
        this.proxyIcebergCatalogFactory = new ProxyIcebergCatalogFactory();
    }

    @PostConstruct
    public void initializeCatalogs() {
        var catalogs = catalogService.list(CatalogType.ICEBERG);

        for (var catalog : catalogs) {
            switch (catalog.catalogMode()) {
                case INTERNAL -> {
                    var instance = kasanariIcebergCatalogFactory.create(
                            catalog.catalogName(),
                            catalog.spec().fileIoProperties(),
                            catalog.spec().catalogProperties()
                    );

                    icebergCatalogs.put(catalog.catalogName(), instance);
                }
                case PROXY -> {
                    var instance = proxyIcebergCatalogFactory.create(
                            catalog.catalogName(),
                            catalog.spec().fileIoProperties(),
                            catalog.spec().catalogProperties()
                    );

                    icebergCatalogs.put(catalog.catalogName(), instance);
                }
            }
        }
    }

    public IcebergCatalogAdapter getOrThrow(String name) {
        var maybeCatalog = icebergCatalogs.get(name);
        if (maybeCatalog == null) {
            throw new NotFoundException("Iceberg catalog wasn't found");
        }

        return maybeCatalog;
    }
}
