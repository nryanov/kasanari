package kasanari.fixtures.s3;

import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

/**
 * MinIO-backed S3 test container. Use {@link #S3FixtureContainer()} when only the host JVM needs the endpoint.
 * <p>
 * For multiple containers that must reach each other, share one {@link Network}:
 * create {@code Network.newNetwork()}, pass it to each fixture constructor, {@link #start()} all containers,
 * then {@link #stop()} them and {@link Network#close()} the network (containers first).
 */
public class S3FixtureContainer {
    private static final int MINIO_S3_INTERNAL_PORT = 9000;
    private static final String DEFAULT_NETWORK_ALIAS = "minio";

    private final MinIOContainer minio;
    private final String dockerNetworkAlias;

    public S3FixtureContainer() {
        this(null, null);
    }

    public S3FixtureContainer(Network network) {
        this(network, DEFAULT_NETWORK_ALIAS);
    }

    public S3FixtureContainer(Network network, String networkAlias) {
        MinIOContainer c = new MinIOContainer(
                DockerImageName
                        .parse("minio/minio:RELEASE.2025-02-28T09-55-16Z")
                        .asCompatibleSubstituteFor("minio")
        );
        String alias = null;
        if (network != null) {
            if (networkAlias == null || networkAlias.isBlank()) {
                throw new IllegalArgumentException("networkAlias must be non-null and non-blank when network is set");
            }
            c = c.withNetwork(network).withNetworkAliases(networkAlias);
            alias = networkAlias;
        }
        this.minio = c;
        this.dockerNetworkAlias = alias;
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

    /**
     * S3 API base URL reachable from other containers on the same {@link Network} (HTTP, internal port {@value #MINIO_S3_INTERNAL_PORT}).
     *
     * @throws IllegalStateException if this fixture was not constructed with a network
     */
    public String urlOnNetwork() {
        if (dockerNetworkAlias == null) {
            throw new IllegalStateException(
                    "urlOnNetwork() requires S3FixtureContainer(Network) or S3FixtureContainer(Network, String)"
            );
        }
        return String.format("http://%s:%d", dockerNetworkAlias, MINIO_S3_INTERNAL_PORT);
    }

    public String username() {
        return minio.getUserName();
    }

    public String password() {
        return minio.getPassword();
    }
}
