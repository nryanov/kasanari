package kasanari.catalog.iceberg.hadoop;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;

import java.util.Map;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapterWithoutNamespaceSupportTest;
import org.apache.hadoop.fs.s3a.Constants;
import org.apache.iceberg.CatalogProperties;

public class HadoopIcebergCatalogTest extends IcebergCatalogAdapterWithoutNamespaceSupportTest {
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
                Constants.AWS_REGION, "none"
        ));
    }
}
