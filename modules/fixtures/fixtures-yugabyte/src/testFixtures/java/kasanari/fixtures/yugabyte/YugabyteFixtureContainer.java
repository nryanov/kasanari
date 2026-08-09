package kasanari.fixtures.yugabyte;

import org.testcontainers.containers.Network;
import org.testcontainers.containers.YugabyteDBYSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * YugabyteDB YSQL fixture (2025.1+) with a colocated application database.
 *
 * <p>Colocation is required for management/security tables that use
 * {@code WITH (colocation = true)}. INTERNAL catalog tables opt out via
 * {@code WITH (colocation = false)} and hash-shard on {@code catalog_name}.
 */
public class YugabyteFixtureContainer {
    public static final String IMAGE = "yugabytedb/yugabyte:2025.1.4.0-b103";
    public static final String COLOCATED_DATABASE = "kasanari";

    private final YugabyteDBYSQLContainer container;
    private boolean colocatedDatabaseReady;

    public YugabyteFixtureContainer() {
        this(null);
    }

    public YugabyteFixtureContainer(Network network) {
        YugabyteDBYSQLContainer yb = new YugabyteDBYSQLContainer(DockerImageName.parse(IMAGE))
                .withDatabaseName("yugabyte")
                .withUsername("yugabyte")
                .withPassword("yugabyte");
        if (network != null) {
            yb = yb.withNetwork(network).withNetworkAliases("yugabyte");
        }
        this.container = yb;
    }

    public void start() {
        container.start();
        ensureColocatedDatabase();
    }

    public void stop() {
        container.stop();
    }

    public String jdbcUrl() {
        ensureColocatedDatabase();
        return container.getJdbcUrl().replace("/" + container.getDatabaseName(), "/" + COLOCATED_DATABASE);
    }

    public String username() {
        return container.getUsername();
    }

    public String password() {
        return container.getPassword();
    }

    public String database() {
        return COLOCATED_DATABASE;
    }

    public Map<String, String> dataSourceProperties() {
        var props = new HashMap<String, String>();
        props.put(kasanari.repository.jdbc.KasanariDataSourceConfiguration.URI, jdbcUrl());
        props.put(kasanari.repository.jdbc.KasanariDataSourceConfiguration.USER, username());
        props.put(kasanari.repository.jdbc.KasanariDataSourceConfiguration.PASSWORD, password());
        props.put(kasanari.repository.jdbc.KasanariDataSourceConfiguration.REPOSITORY_BACKEND, "yugabyte");
        return props;
    }

    private void ensureColocatedDatabase() {
        if (colocatedDatabaseReady) {
            return;
        }
        if (!container.isRunning()) {
            throw new IllegalStateException("Yugabyte container is not running");
        }
        try (var connection = DriverManager.getConnection(
                container.getJdbcUrl(),
                container.getUsername(),
                container.getPassword()
        ); var statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE " + COLOCATED_DATABASE + " WITH COLOCATION = true");
        } catch (SQLException e) {
            if (!e.getMessage().toLowerCase().contains("already exists")) {
                throw new IllegalStateException("Failed to create colocated database " + COLOCATED_DATABASE, e);
            }
        }
        colocatedDatabaseReady = true;
    }
}
