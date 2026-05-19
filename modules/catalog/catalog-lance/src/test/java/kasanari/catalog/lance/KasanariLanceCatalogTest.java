package kasanari.catalog.lance;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.s3.S3FixtureContainer;
import kasanari.fixtures.s3.S3Helper;
import kasanari.repository.jdbc.KasanariDataSourceConfiguration;

import java.util.HashMap;
import java.util.Map;

public class KasanariLanceCatalogTest extends LanceCatalogAdapterTest {
    private final PostgresFixtureContainer postgres = new PostgresFixtureContainer();
    private final S3FixtureContainer s3Container = new S3FixtureContainer();
    private S3Helper s3Helper;

    @Override
    protected LanceCatalogAdapter setupCatalogAdapter() throws Exception {
        s3Container.start();
        postgres.start();

        s3Helper = new S3Helper(s3Container);
        s3Helper.createBucket("warehouse");

        var properties = new HashMap<String, String>();
        properties.put(KasanariDataSourceConfiguration.URI, postgres.jdbcUrl());
        properties.put(KasanariDataSourceConfiguration.USER, postgres.username());
        properties.put(KasanariDataSourceConfiguration.PASSWORD, postgres.password());

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

    @Override
    protected boolean supportsCreateEmptyTable() {
        return true;
    }
}
