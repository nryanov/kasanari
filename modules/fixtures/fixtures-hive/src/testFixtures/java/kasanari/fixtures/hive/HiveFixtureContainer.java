package kasanari.fixtures.hive;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class HiveFixtureContainer {
    private final GenericContainer hive = new GenericContainer<>(DockerImageName.parse("apache/hive:4.0.0"))
            .withExposedPorts(9083)
            .withEnv("SERVICE_NAME", "metastore");

    GenericContainer getHive() {
        if (!hive.isRunning()) {
            throw new RuntimeException("Nessie is not yet started");
        }
        return hive;
    }

    public void start() {
        hive.start();
    }

    public void stop() {
        hive.stop();
    }

    public String thriftUri() {
        return String.format("thrift://%s:%s", hive.getHost(), hive.getMappedPort(9083));
    }
}
