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
    );

    @Override
    public IcebergCatalogAdapter setupCatalog() {
        postgres.start();

        var factory = new KasanariIcebergCatalogFactory();
        return factory.create(Map.of());
    }


    @Override
    public void close() {
        postgres.close();
    }
}
