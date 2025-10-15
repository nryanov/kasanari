package kasanari.catalog.iceberg.jdbc;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogTableApiTest;
import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
import kasanari.fixtures.s3.NoneRegionS3FileIOAwsClientFactory;
import kasanari.fixtures.s3.S3FixtureContainer;
import kasanari.fixtures.s3.S3Helper;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.aws.s3.S3FileIOProperties;

import java.util.HashMap;

public class JdbcIcebergCatalogTableApiTest extends IcebergCatalogTableApiTest {
    private final PostgresFixtureContainer postgres = new PostgresFixtureContainer();
    private final S3FixtureContainer s3Container = new S3FixtureContainer();
    private S3Helper s3Helper;
    private PostgresHelper postgresHelper;

    @Override
    public IcebergCatalogAdapter setupCatalog() {
        postgres.start();
        s3Container.start();

        s3Helper = new S3Helper(s3Container);
        s3Helper.createBucket("warehouse");

        postgresHelper = new PostgresHelper(postgres);

        var properties = new HashMap<String, String>();
        properties.put("jdbc.user", postgres.username());
        properties.put("jdbc.password", postgres.password());
        properties.put(CatalogProperties.URI, postgres.jdbcUrl());
        // view support
        properties.put("jdbc.schema-version", "V1");
        properties.put(CatalogProperties.FILE_IO_IMPL, "org.apache.iceberg.aws.s3.S3FileIO");
        properties.put(CatalogProperties.WAREHOUSE_LOCATION, "s3a://warehouse");
        properties.put(S3FileIOProperties.ENDPOINT, s3Container.url());
        properties.put(S3FileIOProperties.ACCESS_KEY_ID, s3Container.username());
        properties.put(S3FileIOProperties.SECRET_ACCESS_KEY, s3Container.password());
        properties.put(S3FileIOProperties.PATH_STYLE_ACCESS, "true");
        properties.put(S3FileIOProperties.CLIENT_FACTORY, NoneRegionS3FileIOAwsClientFactory.class.getName());

        var factory = new JdbcIcebergCatalogFactory();
        return factory.create(properties);
    }

    @Override
    public void close() {
        postgres.stop();
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
        postgresHelper.truncateTable("iceberg_tables");
        s3Helper.clearBucket("warehouse");
    }
}
