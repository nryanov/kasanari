package kasanari.catalog.lance;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
import kasanari.fixtures.s3.S3FixtureContainer;
import kasanari.fixtures.s3.S3Helper;
import kasanari.repository.jdbc.KasanariDataSourceConfiguration;

import java.util.HashMap;
import java.util.Map;

public class KasanariLanceCatalogTest extends LanceCatalogAdapterTest {
    private final PostgresFixtureContainer postgres = new PostgresFixtureContainer();
    private final S3FixtureContainer s3Container = new S3FixtureContainer();
    private S3Helper s3Helper;
    private PostgresHelper postgresHelper;

    @Override
    protected LanceCatalogAdapter setupCatalogAdapter() throws Exception {
        s3Container.start();
        postgres.start();

        s3Helper = new S3Helper(s3Container);
        s3Helper.createBucket("warehouse");

        postgresHelper = new PostgresHelper(postgres);

        var properties = new HashMap<String, String>();
        properties.put(KasanariDataSourceConfiguration.URI, postgres.jdbcUrl());
        properties.put(KasanariDataSourceConfiguration.USER, postgres.username());
        properties.put(KasanariDataSourceConfiguration.PASSWORD, postgres.password());
        properties.put(KasanariLanceProperties.LOCATION, "s3://warehouse");
        properties.put(KasanariLanceProperties.STORAGE_PROPERTIES_PREFIX + "aws_region", "us-east-1");
        properties.put(KasanariLanceProperties.STORAGE_PROPERTIES_PREFIX + "aws_access_key_id", s3Container.username());
        properties.put(KasanariLanceProperties.STORAGE_PROPERTIES_PREFIX + "aws_secret_access_key", s3Container.password());
        properties.put(KasanariLanceProperties.STORAGE_PROPERTIES_PREFIX + "aws_endpoint", s3Container.url());
        properties.put(KasanariLanceProperties.STORAGE_PROPERTIES_PREFIX + "allow_http", "true");

        var factory = new KasanariLanceCatalogFactory();
        return factory.create("kasanari", Map.of(), properties);
    }

    @Override
    protected String tableLocation() {
        return "s3://warehouse/" + namespaceName + "/" + tableName;
    }

    @Override
    protected void reset() {
        if (s3Helper != null) {
            s3Helper.clearBucket("warehouse");
        }

        postgresHelper.truncateTable("kasanari_lance_tables");
        postgresHelper.truncateTable("kasanari_lance_namespaces");
    }

    @Override
    protected void onClose() {
        postgres.stop();
        s3Container.stop();
    }

    @Override
    protected boolean supportsCreateTable() {
        return true;
    }
}
