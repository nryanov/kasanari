package kasanari.fixtures.s3;

import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

public class S3FixtureContainer {
    private static final int MINIO_S3_INTERNAL_PORT = 9000;
    private static final String NETWORK_ALIAS = "minio";

    private final MinIOContainer minio;

    public S3FixtureContainer() {
        this(null);
    }

    public S3FixtureContainer(Network network) {
        var container = new MinIOContainer(
                DockerImageName
                        .parse("minio/minio:RELEASE.2025-02-28T09-55-16Z")
                        .asCompatibleSubstituteFor("minio")
        );

        if (network != null) {
            container = container.withNetwork(network).withNetworkAliases(NETWORK_ALIAS);
        }

        this.minio = container;
    }

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

    /** S3 API base URL reachable from the host (mapped port). */
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
