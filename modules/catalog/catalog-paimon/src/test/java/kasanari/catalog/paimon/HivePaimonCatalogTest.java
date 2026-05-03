package kasanari.catalog.paimon;

import kasanari.fixtures.hive.HiveFixtureContainer;
import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.s3.S3FixtureContainer;
import kasanari.fixtures.s3.S3Helper;

import java.util.HashMap;

public class HivePaimonCatalogTest extends PaimonCatalogAdapterTest {
    private final HiveFixtureContainer hive = new HiveFixtureContainer();
    private final PostgresFixtureContainer postgres = new PostgresFixtureContainer();
    private final S3FixtureContainer s3Container = new S3FixtureContainer();
    private S3Helper s3Helper;

    @Override
    protected PaimonCatalogAdapter setupCatalogAdapter() {
        hive.start();
        s3Container.start();

        s3Helper = new S3Helper(s3Container);
        s3Helper.createBucket("warehouse");

        var config = new HashMap<String, String>();
        config.put("fs.s3a.access.key", s3Container.username());
        config.put("fs.s3a.secret.key", s3Container.password());
        config.put("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        config.put("fs.s3a.path.style.access", "true");
        config.put("fs.s3a.endpoint", s3Container.url());

        var options = new HashMap<String, String>();
        options.put("type", "jdbc");
        options.put("warehouse", "s3a://warehouse");
        options.put("jdbc-url", postgres.jdbcUrl());
        options.put("jdbc-user", postgres.username());
        options.put("jdbc-password", postgres.password());
        options.put("jdbc-driver", "org.postgresql.Driver");
        options.put("jdbc-table-prefix", "paimon_");

        var factory = new ProxyPaimonCatalogFactory();
        return factory.create(config, options);
    }

    @Override
    protected void reset() {
        s3Helper.clearBucket("warehouse");
    }

    @Override
    protected void close() {
        hive.stop();
        postgres.stop();
        s3Container.stop();
    }
}
