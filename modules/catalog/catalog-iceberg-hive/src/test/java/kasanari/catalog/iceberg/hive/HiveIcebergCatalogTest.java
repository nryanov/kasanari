package kasanari.catalog.iceberg.hive;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapterTest;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class HiveIcebergCatalogTest extends IcebergCatalogAdapterTest {
    private final GenericContainer hive = new GenericContainer<>(DockerImageName.parse("apache/hive:4.0.0"))
            .withExposedPorts(9083)
            .withEnv("SERVICE_NAME", "metastore");

    @Override
    public IcebergCatalogAdapter setupCatalog() {
        hive.start();

        var hiveUri = String.format("thrift://%s:%s", hive.getHost(), hive.getMappedPort(9083));
        var hiveWarehouse = "file:///opt/hive/data/warehouse"; // local

        var factory = new HiveIcebergCatalogFactory();
        return factory.create(Map.of(
                "uri", hiveUri,
                "warehouse", hiveWarehouse
        ));
    }

    @Override
    public void close() {
        hive.close();
    }
}
