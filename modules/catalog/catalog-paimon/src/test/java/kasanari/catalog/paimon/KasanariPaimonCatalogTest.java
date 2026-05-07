package kasanari.catalog.paimon;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
import kasanari.fixtures.s3.S3FixtureContainer;
import kasanari.fixtures.s3.S3Helper;

import java.util.HashMap;

public class KasanariPaimonCatalogTest extends PaimonCatalogAdapterTest {
    private final PostgresFixtureContainer postgres = new PostgresFixtureContainer();
    private final S3FixtureContainer s3Container = new S3FixtureContainer();
    private S3Helper s3Helper;
    private PostgresHelper postgresHelper;

    @Override
    protected PaimonCatalogAdapter setupCatalogAdapter() {
        postgres.start();
        s3Container.start();

        s3Helper = new S3Helper(s3Container);
        s3Helper.createBucket("warehouse");

        postgresHelper = new PostgresHelper(postgres);

        var config = new HashMap<String, String>();
        config.put("fs.s3a.access.key", s3Container.username());
        config.put("fs.s3a.secret.key", s3Container.password());
        config.put("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        config.put("fs.s3a.path.style.access", "true");
        config.put("fs.s3a.endpoint", s3Container.url());

        var options = new HashMap<String, String>();
        options.put("type", "rest");
        options.put("warehouse", "s3a://warehouse");

        var factory = new ProxyPaimonCatalogFactory();
        return factory.create(config, options);
    }

    @Override
    protected void reset() {
        s3Helper.clearBucket("warehouse");
    }

    @Override
    protected void close() {
        postgres.stop();
        s3Container.stop();
    }
}
