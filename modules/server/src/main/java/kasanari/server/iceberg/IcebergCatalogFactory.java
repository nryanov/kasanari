package kasanari.server.iceberg;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import kasanari.catalog.iceberg.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.HadoopIcebergCatalogFactory;
import kasanari.catalog.iceberg.InMemoryIcebergCatalogFactory;
import kasanari.catalog.iceberg.JdbcIcebergCatalogFactory;
import kasanari.catalog.iceberg.KasanariIcebergCatalogFactory;
import kasanari.catalog.iceberg.NessieIcebergCatalogFactory;
import kasanari.catalog.iceberg.RestIcebergCatalogFactory;
import kasanari.server.configuration.IcebergCatalogConfiguration;
import org.jboss.logging.Logger;

@Dependent
public class IcebergCatalogFactory {
    private static final Logger logger = Logger.getLogger(IcebergCatalogFactory.class);

    @Produces
    public IcebergCatalogAdapter icebergCatalogAdapter(IcebergCatalogConfiguration configuration) {
        logger.infof("Chosen iceberg catalog type is %s", configuration.type());
        return switch (configuration.type()) {
            case HIVE -> throw new IllegalArgumentException("Hive catalog not supported yet");
            case JDBC -> new JdbcIcebergCatalogFactory().create(configuration.properties());
            case REST -> new RestIcebergCatalogFactory().create(configuration.properties());
            case HADOOP -> new HadoopIcebergCatalogFactory().create(configuration.properties());
            case NESSIE -> new NessieIcebergCatalogFactory().create(configuration.properties());
            case KASANARI -> new KasanariIcebergCatalogFactory().create(configuration.properties());
            case IN_MEMORY -> new InMemoryIcebergCatalogFactory().create(configuration.properties());
        };
    }
}
