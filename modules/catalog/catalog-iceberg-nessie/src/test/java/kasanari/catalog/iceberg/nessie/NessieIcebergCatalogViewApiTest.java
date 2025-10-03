package kasanari.catalog.iceberg.nessie;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogViewApiTest;
import kasanari.fixtures.s3.NoneRegionS3FileIOAwsClientFactory;
import kasanari.fixtures.s3.S3Container;
import kasanari.fixtures.s3.S3Helper;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.aws.s3.S3FileIOProperties;
import org.projectnessie.testing.nessie.ImmutableNessieConfig;
import org.projectnessie.testing.nessie.NessieContainer;

import java.util.HashMap;
import java.util.UUID;


public class NessieIcebergCatalogViewApiTest extends IcebergCatalogViewApiTest {
    private final NessieContainer nessie = new NessieContainer(
            ImmutableNessieConfig
                    .builder()
                    .dockerImage("ghcr.io/projectnessie/nessie")
                    .dockerTag("0.104.3")
                    .build()
    );

    private final S3Container s3Container = new S3Container();
    private S3Helper s3Helper;

    @Override
    public IcebergCatalogAdapter setupCatalog() {
        nessie.start();
        s3Container.start();

        s3Helper = new S3Helper(s3Container);
        s3Helper.createBucket("warehouse");

        var properties = new HashMap<String, String>();
        properties.put("ref", "main");
        properties.put(CatalogProperties.URI, nessie.getExternalNessieUri().toString());
        // view support
        properties.put("jdbc.schema-version", "V1");
        properties.put(CatalogProperties.FILE_IO_IMPL, "org.apache.iceberg.aws.s3.S3FileIO");
        properties.put(CatalogProperties.WAREHOUSE_LOCATION, "s3a://warehouse");
        properties.put(S3FileIOProperties.ENDPOINT, s3Container.url());
        properties.put(S3FileIOProperties.ACCESS_KEY_ID, s3Container.username());
        properties.put(S3FileIOProperties.SECRET_ACCESS_KEY, s3Container.password());
        properties.put(S3FileIOProperties.PATH_STYLE_ACCESS, "true");
        properties.put(S3FileIOProperties.CLIENT_FACTORY, NoneRegionS3FileIOAwsClientFactory.class.getName());

        var factory = new NessieIcebergCatalogFactory();
        return factory.create(properties);
    }

    @Override
    public void close() {
        nessie.close();
        s3Container.stop();
    }

    @Override
    public String entityLocation(String name) {
        return "s3a://warehouse/" + name;
    }

    @Override
    public String entityName() {
        return "view" + UUID.randomUUID();
    }

    @Override
    public void reset() {
        s3Helper.clearBucket("warehouse");
    }
}
