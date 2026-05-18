package kasanari.catalog.iceberg;

import kasanari.fixtures.hive.HiveFixtureContainer;
import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.catalog.iceberg.s3.NoneRegionS3FileIOAwsClientFactory;
import kasanari.fixtures.s3.S3FixtureContainer;
import kasanari.fixtures.s3.S3Helper;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.aws.s3.S3FileIOProperties;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.hive.HiveCatalog;
import org.testcontainers.containers.Network;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HiveIcebergCatalogTest extends IcebergCatalogAdapterTest {
    private final Network network = Network.newNetwork();
    private final HiveFixtureContainer hive = new HiveFixtureContainer(network);
    private final PostgresFixtureContainer postgres = new PostgresFixtureContainer(network);
    private final S3FixtureContainer s3Container = new S3FixtureContainer(network);
    private S3Helper s3Helper;

    private IcebergCatalogAdapter adapter;

    private final AtomicInteger tableId = new AtomicInteger(1);
    private final AtomicInteger viewId = new AtomicInteger(1);
    private final AtomicInteger namespaceId = new AtomicInteger(10);

    @Override
    public IcebergCatalogAdapter setupCatalog() {
        s3Container.start();
        postgres.start();
        hive.start();

        s3Helper = new S3Helper(s3Container);
        s3Helper.createBucket("warehouse");

        var properties = new HashMap<String, String>();
        properties.put(CatalogProperties.CATALOG_IMPL, HiveCatalog.class.getName());
        properties.put(CatalogProperties.FILE_IO_IMPL, "org.apache.iceberg.aws.s3.S3FileIO");
        properties.put(CatalogProperties.URI, hive.thriftUri());
        properties.put(CatalogProperties.WAREHOUSE_LOCATION, "s3a://warehouse");
        properties.put(S3FileIOProperties.ENDPOINT, s3Container.url());
        properties.put(S3FileIOProperties.ACCESS_KEY_ID, s3Container.username());
        properties.put(S3FileIOProperties.SECRET_ACCESS_KEY, s3Container.password());
        properties.put(S3FileIOProperties.PATH_STYLE_ACCESS, "true");
        properties.put(S3FileIOProperties.CLIENT_FACTORY, NoneRegionS3FileIOAwsClientFactory.class.getName());

        var factory = new ProxyIcebergCatalogFactory();
        this.adapter = factory.create("hive", Map.of(), properties);

        return adapter;
    }

    @Override
    public void reset() {
        s3Helper.clearBucket("warehouse");
    }

    @Override
    public void close() {
        hive.stop();
    }

    @Override
    public String entityLocation(String name) {
        return "s3a://warehouse/" + name;
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
