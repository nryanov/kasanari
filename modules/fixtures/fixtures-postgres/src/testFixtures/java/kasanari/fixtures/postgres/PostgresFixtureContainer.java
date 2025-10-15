package kasanari.fixtures.postgres;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresFixtureContainer {
    private final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.
                    parse("postgres:17")
                    .asCompatibleSubstituteFor("postgres")
    );

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
