package kasanari.server.infrastructure.iceberg;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import kasanari.catalog.iceberg.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.KasanariIcebergCatalogFactory;
import kasanari.catalog.iceberg.ProxyIcebergCatalogFactory;
import kasanari.server.configuration.IcebergCatalogConfiguration;
import org.jboss.logging.Logger;

@Dependent
public class IcebergCatalogFactory {
    private static final Logger logger = Logger.getLogger(IcebergCatalogFactory.class);

    @Produces
    public IcebergCatalogAdapter icebergCatalogAdapter(IcebergCatalogConfiguration configuration) {
        logger.infof("Chosen iceberg catalog type is %s", configuration.type());
        return switch (configuration.type()) {
            case PROXY -> new ProxyIcebergCatalogFactory().create(configuration.name(), configuration.hadoopProperties(), configuration.catalogProperties());
            case KASANARI -> new KasanariIcebergCatalogFactory().create(configuration.name(), configuration.hadoopProperties(), configuration.catalogProperties());
        };
    }
}
