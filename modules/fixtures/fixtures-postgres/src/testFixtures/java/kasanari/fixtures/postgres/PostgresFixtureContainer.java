package kasanari.fixtures.postgres;

import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresFixtureContainer {
    private static final int POSTGRES_INTERNAL_PORT = 5432;
    private static final String NETWORK_ALIAS = "postgres";

    private final PostgreSQLContainer<?> postgres;

    public PostgresFixtureContainer() {
        this(null);
    }

    public PostgresFixtureContainer(Network network) {
        var container = new PostgreSQLContainer<>(
                DockerImageName
                        .parse("postgres:17")
                        .asCompatibleSubstituteFor("postgres")
        );

        if (network != null) {
            container = container.withNetwork(network).withNetworkAliases(NETWORK_ALIAS);
        }

        this.postgres = container;
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
