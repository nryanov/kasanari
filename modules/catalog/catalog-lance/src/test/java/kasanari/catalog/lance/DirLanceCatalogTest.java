package kasanari.catalog.lance;

import kasanari.fixtures.s3.S3FixtureContainer;
import kasanari.fixtures.s3.S3Helper;

import java.util.HashMap;

public class DirLanceCatalogTest extends LanceCatalogAdapterTest {
    private final S3FixtureContainer s3Container = new S3FixtureContainer();
    private S3Helper s3Helper;

    @Override
    protected LanceCatalogAdapter setupCatalogAdapter() {
        s3Container.start();

        s3Helper = new S3Helper(s3Container);
        s3Helper.createBucket("warehouse");

        var properties = new HashMap<String, String>();
        properties.put("root", "s3://warehouse/" + uniqueName("dir_root"));
        properties.put("storage.aws_access_key_id", s3Container.username());
        properties.put("storage.aws_secret_access_key", s3Container.password());
        properties.put("storage.aws_endpoint", s3Container.url());
        properties.put("storage.aws_allow_http", "true");
        properties.put("storage.aws_virtual_hosted_style_request", "false");

        var factory = new ProxyLanceCatalogFactory();
        return factory.create("dir", properties);
    }

    @Override
    protected void reset() {
        if (s3Helper != null) {
            s3Helper.clearBucket("warehouse");
        }
    }

    @Override
    protected void onClose() {
        s3Container.stop();
    }
}
