package kasanari.server.iceberg;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.hadoop.HadoopIcebergCatalogFactory;
import kasanari.catalog.iceberg.inmemory.InMemoryIcebergCatalogFactory;
import kasanari.catalog.iceberg.jdbc.JdbcIcebergCatalogFactory;
import kasanari.catalog.iceberg.kasanari.KasanariIcebergCatalogFactory;
import kasanari.catalog.iceberg.nessie.NessieIcebergCatalogFactory;
import kasanari.catalog.iceberg.rest.RestIcebergCatalogFactory;
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
