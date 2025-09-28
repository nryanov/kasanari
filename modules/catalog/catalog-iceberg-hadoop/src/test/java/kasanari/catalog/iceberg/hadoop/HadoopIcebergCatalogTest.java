package kasanari.catalog.iceberg.hadoop;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapterTest;

import java.util.Map;

import org.apache.hadoop.fs.s3a.Constants;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.aws.s3.S3FileIO;
import org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider;
import org.apache.iceberg.aws.s3.S3FileIOProperties;
import org.apache.hadoop.fs.s3a.S3AFileSystem;
// SimpleAWSCredentialsProvider

public class HadoopIcebergCatalogTest extends IcebergCatalogAdapterTest {
    @Override
    public IcebergCatalogAdapter setupCatalog() {
        var factory = new HadoopIcebergCatalogFactory();
        return factory.create(Map.of(
                CatalogProperties.FILE_IO_IMPL, "org.apache.iceberg.aws.s3.S3FileIO",
                CatalogProperties.WAREHOUSE_LOCATION, "s3a://warehouse",
                Constants.ENDPOINT, "http://localhost:9000",
                Constants.ACCESS_KEY, "admin",
                Constants.SECRET_KEY, "password",
                Constants.PATH_STYLE_ACCESS, "true",
                Constants.SECURE_CONNECTIONS, "false",
//                "fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem",
//                Constants.AWS_CREDENTIALS_PROVIDER, "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider",
                Constants.AWS_REGION, "none"
        ));
    }
}
