package kasanari.catalog.iceberg.hadoop;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;

import java.util.Map;

import kasanari.catalog.iceberg.core.IcebergCatalogTableApiTest;
import kasanari.fixtures.s3.S3Container;
import kasanari.fixtures.s3.S3Helper;
import org.apache.hadoop.fs.s3a.Constants;
import org.apache.iceberg.CatalogProperties;
import org.junit.jupiter.api.Assertions;

public class HadoopIcebergCatalogTableApiTest extends IcebergCatalogTableApiTest {
    private final S3Container s3Container = new S3Container();
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
    public String entityName() {
        return "table";
    }

    @Override
    public void reset() {
        s3Helper.clearBucket("warehouse");
    }

    @Override
    public void successfullyRenameTable() {
        // hadoop catalog doesn't support renaming
        Assertions.assertTrue(true);
    }
}
