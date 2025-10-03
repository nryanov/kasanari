package kasanari.fixtures.s3;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

import java.net.URI;

public class S3Helper {
    private final S3Client s3Client;

    public S3Helper(S3Container container) {
        var minio = container.getMinio();
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
    }

    public void createBucket(String bucket) {
        var rq = CreateBucketRequest.builder().bucket(bucket).build();
        s3Client.createBucket(rq);
    }

    public void clearBucket(String bucket) {
        var listObjectsRq = ListObjectsV2Request
                .builder()
                .bucket(bucket)
                .build();
        var objects = s3Client.listObjectsV2(listObjectsRq);

        objects.contents().forEach(content -> {
            var deleteObjectRq = DeleteObjectRequest
                    .builder()
                    .bucket(bucket)
                    .key(content.key())
                    .build();

            s3Client.deleteObject(deleteObjectRq);
        });
    }
}
