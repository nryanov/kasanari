package kasanari.fixtures.postgres;

import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Postgres test container. Use {@link #PostgresFixtureContainer()} for tests that only talk from the host JVM.
 * <p>
 * For multiple containers that must reach each other, share one {@link Network}:
 * create {@code Network.newNetwork()}, pass it to each fixture constructor, {@link #start()} all containers,
 * then {@link #stop()} them and {@link Network#close()} the network (containers first).
 */
public class PostgresFixtureContainer {
    private static final int POSTGRES_INTERNAL_PORT = 5432;
    private static final String DEFAULT_NETWORK_ALIAS = "postgres";

    private final PostgreSQLContainer<?> postgres;
    private final String dockerNetworkAlias;

    public PostgresFixtureContainer() {
        this(null, null);
    }

    public PostgresFixtureContainer(Network network) {
        this(network, DEFAULT_NETWORK_ALIAS);
    }

    public PostgresFixtureContainer(Network network, String networkAlias) {
        var c = new PostgreSQLContainer<>(
                DockerImageName
                        .parse("postgres:17")
                        .asCompatibleSubstituteFor("postgres")
        );
        String alias = null;
        if (network != null) {
            if (networkAlias == null || networkAlias.isBlank()) {
                throw new IllegalArgumentException("networkAlias must be non-null and non-blank when network is set");
            }
            c = c.withNetwork(network).withNetworkAliases(networkAlias);
            alias = networkAlias;
        }
        this.postgres = c;
        this.dockerNetworkAlias = alias;
    }

    PostgreSQLContainer<?> getPostgres() {
        if (!postgres.isRunning()) {
            throw new RuntimeException("Postgres is not yet started");
        }
        return postgres;
    }

    public void start() {
        postgres.start();
    }

    public void stop() {
        postgres.stop();
    }

    /** JDBC URL reachable from the host (mapped port). */
    public String jdbcUrl() {
        return postgres.getJdbcUrl();
    }

    /**
     * JDBC URL reachable from other containers on the same {@link Network} (alias and internal port {@value #POSTGRES_INTERNAL_PORT}).
     *
     * @throws IllegalStateException if this fixture was not constructed with a network
     */
    public String jdbcUrlOnNetwork() {
        if (dockerNetworkAlias == null) {
            throw new IllegalStateException(
                    "jdbcUrlOnNetwork() requires PostgresFixtureContainer(Network) or PostgresFixtureContainer(Network, String)"
            );
        }
        return String.format(
                "jdbc:postgresql://%s:%d/%s",
                dockerNetworkAlias,
                POSTGRES_INTERNAL_PORT,
                postgres.getDatabaseName()
        );
    }

    public String username() {
        return postgres.getUsername();
    }

    public String password() {
        return postgres.getPassword();
    }

    public String database() {
        return postgres.getDatabaseName();
    }
}
