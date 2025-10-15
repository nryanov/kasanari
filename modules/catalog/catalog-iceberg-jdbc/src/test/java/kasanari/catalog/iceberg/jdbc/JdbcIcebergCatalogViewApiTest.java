package kasanari.catalog.iceberg.jdbc;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogCommons;
import kasanari.catalog.iceberg.core.IcebergCatalogViewApiTest;
import kasanari.fixtures.s3.NoneRegionS3FileIOAwsClientFactory;
import kasanari.fixtures.s3.S3FixtureContainer;
import kasanari.fixtures.s3.S3Helper;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.aws.s3.S3FileIOProperties;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcIcebergCatalogViewApiTest extends IcebergCatalogViewApiTest {
    private final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.
                    parse("postgres:17")
                    .asCompatibleSubstituteFor("postgres")
    );

    private final S3FixtureContainer s3Container = new S3FixtureContainer();
    private S3Helper s3Helper;

    @Override
    public IcebergCatalogAdapter setupCatalog() {
        postgres.start();
        s3Container.start();

        s3Helper = new S3Helper(s3Container);
        s3Helper.createBucket("warehouse");

        var properties = new HashMap<String, String>();
        properties.put("jdbc.user", postgres.getUsername());
        properties.put("jdbc.password", postgres.getPassword());
        properties.put(CatalogProperties.URI, postgres.getJdbcUrl());
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
        postgres.close();
        s3Container.stop();
    }

    @Override
    public String entityLocation(String name) {
        return "s3a://warehouse/" + name;
    }

    @Override
    public String entityName() {
        return "view";
    }

    @Override
    public void reset() {
        try {
            postgres.execInContainer("psql", "-U", postgres.getUsername(), "-d", postgres.getDatabaseName(), "-c", "TRUNCATE iceberg_tables");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        s3Helper.clearBucket("warehouse");
    }

    @Override
    public void returnNonEmptyListOfViews() {
        var namespace = Namespace.empty();
        var viewName = entityName();
        var rq = IcebergCatalogCommons.defaultCreateViewRequest(namespace, viewName, entityLocation(viewName));

        catalog.createView(namespace, rq);

        var result = catalog.listViews(namespace, null, 10);

        // returned view will have name `.view`
        var expectedViews = List.of(TableIdentifier.of(namespace, "." + viewName));
        assertEquals(expectedViews, result.identifiers());
    }
}
