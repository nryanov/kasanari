package kasanari.catalog.iceberg.kasanari;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapterTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class KasanariIcebergCatalogTest extends IcebergCatalogAdapterTest {
    private final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.
                    parse("postgres:17")
                    .asCompatibleSubstituteFor("postgres")
    )
            .withUsername("postgres")
            .withPassword("postgres")
            .withDatabaseName("kasanari");

    @Override
    public IcebergCatalogAdapter setupCatalog() {
//        postgres.start();

        var factory = new KasanariIcebergCatalogFactory();
//        return factory.create(Map.of(
//                KasanariCatalogProperties.WAREHOUSE, "file:///tmp/iceberg-kasanari-catalog-warehouse",
//                KasanariCatalogProperties.URI, postgres.getJdbcUrl(),
//                KasanariCatalogProperties.USER, postgres.getUsername(),
//                KasanariCatalogProperties.PASSWORD, postgres.getPassword()
//        ));

        return factory.create(Map.of(
                KasanariCatalogProperties.WAREHOUSE, "file:///tmp/iceberg-kasanari-catalog-warehouse",
                KasanariCatalogProperties.URI, "jdbc:postgresql://localhost:5432/postgres",
                KasanariCatalogProperties.USER, "postgres",
                KasanariCatalogProperties.PASSWORD, "postgres"
        ));
    }

    @Override
    public void close() {
//        postgres.close();
    }

    @Override
    public void reset() {
        var catalogDelegate = (KasanariCatalog) catalog.delegate();
        catalogDelegate.getDataSource().getJdbi().useTransaction(tx -> {
            tx.execute("TRUNCATE TABLE kasanari_iceberg_namespace_properties CASCADE");
            tx.execute("TRUNCATE TABLE kasanari_iceberg_tables CASCADE");
            tx.execute("TRUNCATE TABLE kasanari_iceberg_views CASCADE");
            tx.execute("TRUNCATE TABLE kasanari_iceberg_namespaces CASCADE");
        });
    }
}
