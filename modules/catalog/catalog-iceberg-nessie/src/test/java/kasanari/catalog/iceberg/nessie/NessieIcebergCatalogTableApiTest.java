package kasanari.catalog.iceberg.nessie;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogTableApiTest;
import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.aws.s3.S3FileIOProperties;
import org.projectnessie.testing.nessie.ImmutableNessieConfig;
import org.projectnessie.testing.nessie.NessieContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

import java.net.URI;
import java.util.HashMap;
import java.util.UUID;

public class NessieIcebergCatalogTableApiTest extends IcebergCatalogTableApiTest {
    private final NessieContainer nessie = new NessieContainer(
            ImmutableNessieConfig
                    .builder()
                    .dockerImage("ghcr.io/projectnessie/nessie")
                    .dockerTag("0.104.3")
                    .build()
    );

    private final MinIOContainer minio = new MinIOContainer(
            DockerImageName.
                    parse("minio/minio:RELEASE.2025-02-28T09-55-16Z")
                    .asCompatibleSubstituteFor("minio")
    );

    private S3Client s3Client;

    @Override
    public IcebergCatalogAdapter setupCatalog() {
        nessie.start();
        minio.start();

        this.s3Client = S3Client
                .builder()
                .endpointOverride(URI.create(minio.getS3URL()))
                .forcePathStyle(true)
                .region(Region.of("none"))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        minio.getUserName(),
                                        minio.getPassword())
                        )
                )
                .build();

        this.s3Client.createBucket(CreateBucketRequest.builder().bucket("warehouse").build());

        var properties = new HashMap<String, String>();
        properties.put("ref", "main");
        properties.put(CatalogProperties.URI, nessie.getExternalNessieUri().toString());
        // view support
        properties.put("jdbc.schema-version", "V1");
        properties.put(CatalogProperties.FILE_IO_IMPL, "org.apache.iceberg.aws.s3.S3FileIO");
        properties.put(CatalogProperties.WAREHOUSE_LOCATION, "s3a://warehouse");
        properties.put(S3FileIOProperties.ENDPOINT, minio.getS3URL());
        properties.put(S3FileIOProperties.ACCESS_KEY_ID, minio.getUserName());
        properties.put(S3FileIOProperties.SECRET_ACCESS_KEY, minio.getPassword());
        properties.put(S3FileIOProperties.PATH_STYLE_ACCESS, "true");
        properties.put(S3FileIOProperties.CLIENT_FACTORY, "kasanari.catalog.iceberg.nessie.NoneRegionS3FileIOAwsClientFactory");

        var factory = new NessieIcebergCatalogFactory();
        return factory.create(properties);
    }

    @Override
    public void close() {
        nessie.close();
        minio.close();
    }

    @Override
    public String entityLocation(String name) {
        return "s3a://warehouse/" + name;
    }

    @Override
    public String entityName() {
        return "table_" + UUID.randomUUID();
    }

    @Override
    public void reset() {
        var listObjectsRq = ListObjectsV2Request
                .builder()
                .bucket("warehouse")
                .build();
        var objects = s3Client.listObjectsV2(listObjectsRq);

        objects.contents().forEach(content -> {
            var deleteObjectRq = DeleteObjectRequest
                    .builder()
                    .bucket("warehouse")
                    .key(content.key())
                    .build();

            s3Client.deleteObject(deleteObjectRq);
        });
    }
}
