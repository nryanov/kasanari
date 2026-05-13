package kasanari.catalog.lance;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.s3.S3FixtureContainer;
import kasanari.fixtures.s3.S3Helper;
import org.junit.jupiter.api.Disabled;

import java.util.HashMap;

@Disabled("Fixes required")
public class Hive2LanceCatalogTest extends LanceCatalogAdapterTest {
    private final PostgresFixtureContainer postgres = new PostgresFixtureContainer();
    private final S3FixtureContainer s3Container = new S3FixtureContainer();
    private S3Helper s3Helper;

    @Override
    protected LanceCatalogAdapter setupCatalogAdapter() {
        s3Container.start();
        postgres.start();

        s3Helper = new S3Helper(s3Container);
        s3Helper.createBucket("warehouse");

        // TODO: setup postgres connection via Hadoop#Configuration
        var properties = new HashMap<String, String>();
        properties.put("root", "s3://warehouse/" + uniqueName("hive2_root"));
        properties.put("client.pool-size", "2");
        properties.put("fs.s3a.access.key", s3Container.username());
        properties.put("fs.s3a.secret.key", s3Container.password());
        properties.put("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        properties.put("fs.s3a.path.style.access", "true");
        properties.put("fs.s3a.endpoint", s3Container.url());

        var factory = new ProxyLanceCatalogFactory();
        return factory.create("hive2", properties);
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
}
