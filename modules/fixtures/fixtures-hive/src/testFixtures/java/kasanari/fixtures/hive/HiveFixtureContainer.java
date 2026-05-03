package kasanari.fixtures.hive;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

/**
 * Hive metastore test container. Use {@link #HiveFixtureContainer()} when only the host JVM needs the Thrift URI.
 * <p>
 * For multiple containers that must reach each other, share one {@link Network}:
 * create {@code Network.newNetwork()}, pass it to each fixture constructor, {@link #start()} all containers,
 * then {@link #stop()} them and {@link Network#close()} the network (containers first).
 */
public class HiveFixtureContainer {
    private static final int METASTORE_PORT = 9083;
    private static final String DEFAULT_NETWORK_ALIAS = "hive";

    private final GenericContainer<?> hive;
    private final String dockerNetworkAlias;

    public HiveFixtureContainer() {
        this(null, null);
    }

    public HiveFixtureContainer(Network network) {
        this(network, DEFAULT_NETWORK_ALIAS);
    }

    public HiveFixtureContainer(Network network, String networkAlias) {
        GenericContainer<?> c = new GenericContainer<>(DockerImageName.parse("nryanov/tools-hive:sha-0c88087d07bb7adcc9ecc7475c653f96bb5de833"))
                .withExposedPorts(METASTORE_PORT)
                .withEnv("SERVICE_NAME", "metastore");
        String alias = null;
        if (network != null) {
            if (networkAlias == null || networkAlias.isBlank()) {
                throw new IllegalArgumentException("networkAlias must be non-null and non-blank when network is set");
            }
            c = c.withNetwork(network).withNetworkAliases(networkAlias);
            alias = networkAlias;
        }
        this.hive = c;
        this.dockerNetworkAlias = alias;
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

    /**
     * Thrift URI reachable from other containers on the same {@link Network} (internal port {@value #METASTORE_PORT}).
     *
     * @throws IllegalStateException if this fixture was not constructed with a network
     */
    public String thriftUriOnNetwork() {
        if (dockerNetworkAlias == null) {
            throw new IllegalStateException(
                    "thriftUriOnNetwork() requires HiveFixtureContainer(Network) or HiveFixtureContainer(Network, String)"
            );
        }
        return String.format("thrift://%s:%d", dockerNetworkAlias, METASTORE_PORT);
    }
}
