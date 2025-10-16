package kasanari.catalog.iceberg.hive;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapterTest;
import org.apache.iceberg.catalog.Namespace;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HiveIcebergCatalogTest extends IcebergCatalogAdapterTest {
    private final GenericContainer hive = new GenericContainer<>(DockerImageName.parse("apache/hive:4.0.0"))
            .withExposedPorts(9083)
            .withEnv("SERVICE_NAME", "metastore");

    private IcebergCatalogAdapter adapter;

    private final AtomicInteger tableId = new AtomicInteger(1);
    private final AtomicInteger viewId = new AtomicInteger(1);
    private final AtomicInteger namespaceId = new AtomicInteger(10);

    @Override
    public IcebergCatalogAdapter setupCatalog() {
        hive.start();

        var hiveUri = String.format("thrift://%s:%s", hive.getHost(), hive.getMappedPort(9083));
        var hiveWarehouse = "file:///tmp/warehouse"; // local

        var factory = new HiveIcebergCatalogFactory();
        this.adapter = factory.create(Map.of(
                "uri", hiveUri,
                "warehouse", hiveWarehouse
        ));

        return adapter;
    }

    @Override
    public void close() {
        hive.close();
    }

    @Override
    public String entityLocation(String name) {
        return "file:///tmp/" + name;
    }

    @Override
    public String tableName() {
        return "table" + tableId.getAndIncrement();
    }

    @Override
    public String viewName() {
        return "view" + viewId.getAndIncrement();
    }

    @Override
    public Namespace namespaceName() {
        // hive requires that each entity has namespace
        var ns = Namespace.of("ns_" + namespaceId.getAndIncrement());
        adapter.createNamespace(ns);
        return ns;
    }
}
