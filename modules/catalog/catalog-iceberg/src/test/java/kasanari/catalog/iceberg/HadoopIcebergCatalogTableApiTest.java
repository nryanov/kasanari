package kasanari.catalog.iceberg;

import kasanari.fixtures.s3.S3FixtureContainer;
import kasanari.fixtures.s3.S3Helper;
import org.apache.hadoop.fs.s3a.Constants;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.catalog.Namespace;
import org.junit.jupiter.api.Assumptions;

import java.util.Map;

public class HadoopIcebergCatalogTableApiTest extends IcebergCatalogAdapterTest {
    private final S3FixtureContainer s3Container = new S3FixtureContainer();
    private S3Helper s3Helper;

    @Override
    public IcebergCatalogAdapter setupCatalog() {
        s3Container.start();
        s3Helper = new S3Helper(s3Container);

        s3Helper.createBucket("warehouse");

        var factory = new HadoopIcebergCatalogFactory();
        return factory.create(Map.of(
                CatalogProperties.FILE_IO_IMPL, "org.apache.iceberg.aws.s3.S3FileIO",
                CatalogProperties.WAREHOUSE_LOCATION, "s3a://warehouse",
                Constants.ENDPOINT, s3Container.url(),
                Constants.ACCESS_KEY,  s3Container.username(),
                Constants.SECRET_KEY, s3Container.password(),
                Constants.PATH_STYLE_ACCESS, "true",
                Constants.SECURE_CONNECTIONS, "false",
                Constants.AWS_REGION, "none"
        ));
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
