package kasanari.catalog.iceberg.jdbc;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapterTest;
import org.apache.iceberg.CatalogProperties;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class JdbcIcebergCatalogTest extends IcebergCatalogAdapterTest {
    private final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.
                    parse("postgres:17")
                    .asCompatibleSubstituteFor("postgres")
    );

    @Override
    public IcebergCatalogAdapter setupCatalog() {
        postgres.start();

        var factory = new JdbcIcebergCatalogFactory();
        return factory.create(Map.of(
                "jdbc.user", postgres.getUsername(),
                "jdbc.password", postgres.getPassword(),
                CatalogProperties.WAREHOUSE_LOCATION, "file:///tmp/jdbc-catalog-test",
                CatalogProperties.URI, postgres.getJdbcUrl()
        ));
    }

    @Override
    public void close() {
        postgres.close();
    }
}
