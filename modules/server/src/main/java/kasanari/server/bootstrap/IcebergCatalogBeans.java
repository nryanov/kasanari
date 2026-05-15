package kasanari.server.bootstrap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import kasanari.catalog.iceberg.IcebergCatalogConfigurationService;

@ApplicationScoped
public class IcebergCatalogBeans {
    @Singleton
    @Produces
    IcebergCatalogConfigurationService icebergCatalogConfigurationService() {
        return new IcebergCatalogConfigurationService();
    }
}
