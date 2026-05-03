package kasanari.fixtures.hive;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

public class HiveFixtureContainer {
    private static final int METASTORE_PORT = 9083;
    private static final String NETWORK_ALIAS = "hive";

    private final GenericContainer<?> hive;

    public HiveFixtureContainer() {
        this(null);
    }

    public HiveFixtureContainer(Network network) {
        var container = new GenericContainer<>(DockerImageName.parse("nryanov/tools-hive:sha-0c88087d07bb7adcc9ecc7475c653f96bb5de833"))
                .withExposedPorts(METASTORE_PORT)
                .withEnv("SERVICE_NAME", "metastore");

        if (network != null) {
            container = container.withNetwork(network).withNetworkAliases(NETWORK_ALIAS);
        }
        this.hive = container;
    }

    public GenericContainer<?> getHive() {
        if (!hive.isRunning()) {
            throw new RuntimeException("Hive is not started yet");
        }
        return hive;
    }

    public void start() {
        hive.start();
    }

    public void stop() {
        hive.stop();
    }

    /** Thrift URI reachable from the host (mapped port). */
    public String thriftUri() {
        return String.format("thrift://%s:%s", hive.getHost(), hive.getMappedPort(METASTORE_PORT));
    }
}
