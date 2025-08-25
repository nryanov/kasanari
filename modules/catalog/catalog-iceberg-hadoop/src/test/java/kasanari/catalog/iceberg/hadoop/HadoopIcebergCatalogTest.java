package kasanari.catalog.iceberg.hadoop;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapterTest;

import java.util.Map;

import org.apache.hadoop.fs.s3a.S3AFileSystem;
import org.apache.iceberg.aws.s3.S3FileIO;
// SimpleAWSCredentialsProvider

public class HadoopIcebergCatalogTest extends IcebergCatalogAdapterTest {
    @Override
    public IcebergCatalogAdapter setupCatalog() {
        S3AFileSystem a = null;

        var factory = new HadoopIcebergCatalogFactory();
        return factory.create(Map.of(
                "io-impl", "org.apache.iceberg.aws.s3.S3FileIO",
                "warehouse", "s3a://warehouse",
                "s3.endpoint", "http://localhost:9001",
                "s3.access-key-id", "admin",
                "s3.secret-access-key", "password",
                "s3.path-style-access", "true"
        ));
    }
}
