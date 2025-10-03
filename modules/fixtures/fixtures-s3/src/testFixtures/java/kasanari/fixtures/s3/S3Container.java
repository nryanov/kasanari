package kasanari.fixtures.s3;

import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;

public class S3Container {
    private final MinIOContainer minio = new MinIOContainer(
            DockerImageName.
                    parse("minio/minio:RELEASE.2025-02-28T09-55-16Z")
                    .asCompatibleSubstituteFor("minio")
    );

    MinIOContainer getMinio() {
        if (!minio.isRunning()) {
            throw new RuntimeException("S3 is not yet started");
        }
        return minio;
    }

    public void start() {
        minio.start();
    }

    public void stop() {
        minio.stop();
    }

    public String url() {
        return minio.getS3URL();
    }

    public String username() {
        return minio.getUserName();
    }

    public String password() {
        return minio.getPassword();
    }
}
