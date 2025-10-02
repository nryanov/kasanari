package kasanari.catalog.iceberg.hadoop;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;

import java.net.URI;
import java.util.Map;

import kasanari.catalog.iceberg.core.IcebergCatalogTableApiTest;
import org.apache.hadoop.fs.s3a.Constants;
import org.apache.iceberg.CatalogProperties;
import org.junit.jupiter.api.Assertions;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

public class HadoopIcebergCatalogTableApiTest extends IcebergCatalogTableApiTest {
    private final MinIOContainer minio = new MinIOContainer(
            DockerImageName.
                    parse("minio/minio:RELEASE.2025-02-28T09-55-16Z")
                    .asCompatibleSubstituteFor("minio")
    );

    private S3Client s3Client;

    @Override
    public IcebergCatalogAdapter setupCatalog() {
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

        var factory = new HadoopIcebergCatalogFactory();
        return factory.create(Map.of(
                CatalogProperties.FILE_IO_IMPL, "org.apache.iceberg.aws.s3.S3FileIO",
                CatalogProperties.WAREHOUSE_LOCATION, "s3a://warehouse",
                Constants.ENDPOINT, minio.getS3URL(),
                Constants.ACCESS_KEY,  minio.getUserName(),
                Constants.SECRET_KEY, minio.getPassword(),
                Constants.PATH_STYLE_ACCESS, "true",
                Constants.SECURE_CONNECTIONS, "false",
                Constants.AWS_REGION, "none"
        ));
    }

    @Override
    public void close() {
        minio.stop();
    }

    @Override
    public String entityLocation(String name) {
        return "s3a://warehouse/" + name;
    }

    @Override
    public String entityName() {
        return "table";
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

    @Override
    public void successfullyRenameTable() {
        // hadoop catalog doesn't support renaming
        Assertions.assertTrue(true);
    }
}
