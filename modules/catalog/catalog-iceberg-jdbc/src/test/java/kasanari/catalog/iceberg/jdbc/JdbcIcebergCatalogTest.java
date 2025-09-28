package kasanari.catalog.iceberg.jdbc;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapterTest;
import org.apache.hadoop.fs.s3a.Constants;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.aws.s3.S3FileIOProperties;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

public class JdbcIcebergCatalogTest extends IcebergCatalogAdapterTest {
    private final PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.
                    parse("postgres:17")
                    .asCompatibleSubstituteFor("postgres")
    );

    @Override
    public IcebergCatalogAdapter setupCatalog() {
//        postgres.start();

        var properties = new HashMap<String, String>();
//        properties.put("jdbc.user", postgres.getUsername());
        properties.put("jdbc.user", "postgres");
//        properties.put("jdbc.password", postgres.getPassword());
        properties.put("jdbc.password", "postgres");
//        properties.put(CatalogProperties.URI, postgres.getJdbcUrl());
        properties.put(CatalogProperties.URI, "jdbc:postgresql://localhost:5432/postgres");
        // view support
        properties.put("jdbc.schema-version", "V1");
        properties.put(CatalogProperties.FILE_IO_IMPL, "org.apache.iceberg.aws.s3.S3FileIO");
        properties.put(CatalogProperties.WAREHOUSE_LOCATION, "s3a://warehouse");
        properties.put(S3FileIOProperties.ENDPOINT, "http://localhost:9000");
        properties.put(S3FileIOProperties.ACCESS_KEY_ID, "admin");
        properties.put(S3FileIOProperties.SECRET_ACCESS_KEY, "password");
        properties.put(S3FileIOProperties.PATH_STYLE_ACCESS, "true");
        properties.put("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");

        var factory = new JdbcIcebergCatalogFactory();
        return factory.create(properties);
    }

    @Override
    public void close() {
//        postgres.close();
    }
}
