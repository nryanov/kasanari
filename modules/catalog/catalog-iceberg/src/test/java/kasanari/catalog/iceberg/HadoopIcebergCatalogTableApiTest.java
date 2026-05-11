package kasanari.catalog.iceberg;

import kasanari.fixtures.s3.NoneRegionS3FileIOAwsClientFactory;
import kasanari.fixtures.s3.S3FixtureContainer;
import kasanari.fixtures.s3.S3Helper;
import org.apache.hadoop.fs.s3a.Constants;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.aws.s3.S3FileIOProperties;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.hadoop.HadoopCatalog;
import org.junit.jupiter.api.Assumptions;

import java.util.HashMap;
import java.util.Map;

public class HadoopIcebergCatalogTableApiTest extends IcebergCatalogAdapterTest {
    private final S3FixtureContainer s3Container = new S3FixtureContainer();
    private S3Helper s3Helper;

    @Override
    public IcebergCatalogAdapter setupCatalog() {
        s3Container.start();
        s3Helper = new S3Helper(s3Container);

        s3Helper.createBucket("warehouse");

        var hadoopProperties = new HashMap<String, String>();
        var catalogProperties = new HashMap<String, String>();

        hadoopProperties.put(Constants.ENDPOINT, s3Container.url());
        hadoopProperties.put(Constants.ACCESS_KEY, s3Container.username());
        hadoopProperties.put(Constants.SECRET_KEY, s3Container.password());
        hadoopProperties.put(Constants.PATH_STYLE_ACCESS, "true");
        hadoopProperties.put(Constants.SECURE_CONNECTIONS, "false");
        hadoopProperties.put(Constants.AWS_REGION, "none");

        catalogProperties.put(CatalogProperties.CATALOG_IMPL, HadoopCatalog.class.getName());
        catalogProperties.put(CatalogProperties.FILE_IO_IMPL, "org.apache.iceberg.aws.s3.S3FileIO");
        catalogProperties.put(CatalogProperties.WAREHOUSE_LOCATION, "s3a://warehouse");
        catalogProperties.put(S3FileIOProperties.ENDPOINT, s3Container.url());
        catalogProperties.put(S3FileIOProperties.ACCESS_KEY_ID, s3Container.username());
        catalogProperties.put(S3FileIOProperties.SECRET_ACCESS_KEY, s3Container.password());
        catalogProperties.put(S3FileIOProperties.PATH_STYLE_ACCESS, "true");
        catalogProperties.put(S3FileIOProperties.CLIENT_FACTORY, NoneRegionS3FileIOAwsClientFactory.class.getName());

        var factory = new ProxyIcebergCatalogFactory();
        return factory.create("hdfs", hadoopProperties, catalogProperties);
    }

    @Override
    public void close() {
        s3Container.stop();
    }

    @Override
    public String entityLocation(String name) {
        return "s3a://warehouse/" + name;
    }

    @Override
    public String tableName() {
        return "table";
    }

    @Override
    public String viewName() {
        return "view";
    }

    @Override
    public Namespace namespaceName() {
        return Namespace.empty();
    }

    @Override
    public boolean isNamespaceSupported() {
        return false;
    }

    @Override
    public boolean isViewSupported() {
        return false;
    }

    @Override
    public void reset() {
        s3Helper.clearBucket("warehouse");
    }

    @Override
    public void successfullyRenameTable() {
        // hadoop catalog doesn't support renaming
        Assumptions.abort("Test skipped: hadoop catalog doesn't support table renaming");
    }
}
