package kasanari.catalog.lance;

import kasanari.fixtures.s3.S3FixtureContainer;
import kasanari.fixtures.s3.S3Helper;
import org.junit.jupiter.api.Disabled;
import org.lance.namespace.model.CreateTableRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Disabled
public class DirLanceCatalogTest extends LanceCatalogAdapterTest {
    private final S3FixtureContainer s3Container = new S3FixtureContainer();
    private S3Helper s3Helper;

    @Override
    protected LanceCatalogAdapter setupCatalogAdapter() {
        s3Container.start();

        s3Helper = new S3Helper(s3Container);
        s3Helper.createBucket("warehouse");

        var properties = new HashMap<String, String>();
        properties.put("root", "s3://warehouse");
        properties.put("manifest_enabled", "true");
        properties.put("dir_listing_enabled", "true");
        properties.put("storage.aws_access_key_id", s3Container.username());
        properties.put("storage.aws_secret_key", s3Container.password());
        properties.put("storage.aws_endpoint", s3Container.url());
        properties.put("storage.aws_allow_http", "true");
        properties.put("storage.aws_virtual_hosted_style_request", "false");
        properties.put("storage.access_key_id", s3Container.username());
        properties.put("storage.secret_access_key", s3Container.password());
        properties.put("storage.endpoint", s3Container.url());
        properties.put("storage.allow_http", "true");
        properties.put("storage.virtual_hosted_style_request", "false");
        properties.put("storage.region", "us-east-1");

        var factory = new ProxyLanceCatalogFactory();
        return factory.create("dir", Map.of(), properties);
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

    @Override
    protected List<String> namespaceId() {
        return List.of();
    }

    @Override
    protected List<String> tableId() {
        return List.of(tableName);
    }

    @Override
    protected void createNamespaceEntity() {
    }

    @Override
    protected void registerTableEntity() {
        adapter.createTable(
                new CreateTableRequest().id(tableId()).properties(Map.of("stage", "created")),
                LanceArrowIpc.emptyBatch());
    }

    @Override
    protected boolean supportsCreateNamespace() {
        return false;
    }

    @Override
    protected boolean supportsDescribeNamespace() {
        return false;
    }

    @Override
    protected boolean supportsDropNamespace() {
        return false;
    }

    @Override
    protected boolean supportsListNamespaces() {
        return false;
    }

    @Override
    protected boolean supportsRegisterTable() {
        return false;
    }

    @Override
    protected boolean supportsCreateTable() {
        return false;
    }

    @Override
    protected boolean supportsCreateEmptyTable() {
        return false;
    }

    @Override
    protected boolean supportsDeregisterTable() {
        return false;
    }
}
