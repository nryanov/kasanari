package kasanari.catalog.paimon.repository.jdbc;

import io.agroal.api.configuration.AgroalConnectionFactoryConfiguration;
import io.agroal.api.configuration.supplier.AgroalConnectionFactoryConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalConnectionPoolConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.agroal.pool.DataSource;
import org.jdbi.v3.core.Jdbi;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

public class KasanariDataSource implements Closeable {
    public static final String URI = "kasanari.jdbc.uri";
    public static final String USER = "kasanari.jdbc.user";
    public static final String PASSWORD = "kasanari.jdbc.password";
    public static final String CONNECTION_POOL_INITIAL_SIZE = "kasanari.jdbc.connection-pool.initial-size";
    public static final String CONNECTION_POOL_MIN_SIZE = "kasanari.jdbc.connection-pool.min-size";
    public static final String CONNECTION_POOL_MAX_SIZE = "kasanari.jdbc.connection-pool.max-size";
    public static final String CONNECTION_POOL_MAX_LIFETIME_MILLIS = "kasanari.jdbc.connection-pool.max-lifetime.millis";

    private static final int CONNECTION_POOL_INITIAL_SIZE_DEFAULT = 2;
    private static final int CONNECTION_POOL_MIN_SIZE_DEFAULT = 1;
    private static final int CONNECTION_POOL_MAX_SIZE_DEFAULT = 5;
    private static final Duration CONNECTION_POOL_MAX_LIFETIME_MILLIS_DEFAULT = Duration.ZERO;

    private final DataSource pool;
    private final Jdbi jdbi;

    public KasanariDataSource(Map<String, String> options) {
        var properties = new DataSourceProperties(options);

        var connectionFactory = new AgroalConnectionFactoryConfigurationSupplier();
        connectionFactory.jdbcUrl(properties.getUri());
        connectionFactory.principal(new NamePrincipal(properties.getUser()));
        connectionFactory.credential(new SimplePassword(properties.getPassword()));
        connectionFactory.jdbcTransactionIsolation(AgroalConnectionFactoryConfiguration.TransactionIsolation.READ_COMMITTED);

        var connectionPool = new AgroalConnectionPoolConfigurationSupplier();
        connectionPool.initialSize(properties.getConnectionPoolInitialSize());
        connectionPool.minSize(properties.getConnectionPoolMinSize());
        connectionPool.maxSize(properties.getConnectionPoolMaxSize());
        connectionPool.maxLifetime(properties.getConnectionPoolMaxLifetimeMillis());
        connectionPool.connectionFactoryConfiguration(connectionFactory);

        var dataSourceConfiguration = new AgroalDataSourceConfigurationSupplier();
        dataSourceConfiguration.connectionPoolConfiguration(connectionPool);
        dataSourceConfiguration.metricsEnabled(false);

        this.pool = new DataSource(dataSourceConfiguration.get());
        this.jdbi = Jdbi.create(pool);
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    @Override
    public void close() throws IOException {
        pool.close();
    }

    private record DataSourceProperties(Map<String, String> options) {
        private String getUser() {
            return getRequired(USER);
        }

        private String getPassword() {
            return getRequired(PASSWORD);
        }

        private String getUri() {
            return getRequired(URI);
        }

        private int getConnectionPoolInitialSize() {
            return getOrDefault(CONNECTION_POOL_INITIAL_SIZE, CONNECTION_POOL_INITIAL_SIZE_DEFAULT, Integer::parseInt);
        }

        private int getConnectionPoolMinSize() {
            return getOrDefault(CONNECTION_POOL_MIN_SIZE, CONNECTION_POOL_MIN_SIZE_DEFAULT, Integer::parseInt);
        }

        private int getConnectionPoolMaxSize() {
            return getOrDefault(CONNECTION_POOL_MAX_SIZE, CONNECTION_POOL_MAX_SIZE_DEFAULT, Integer::parseInt);
        }

        private Duration getConnectionPoolMaxLifetimeMillis() {
            return getOrDefault(
                    CONNECTION_POOL_MAX_LIFETIME_MILLIS,
                    CONNECTION_POOL_MAX_LIFETIME_MILLIS_DEFAULT,
                    value -> Duration.ofMillis(Long.parseLong(value))
            );
        }

        private String getRequired(String key) {
            var value = options.get(key);
            if (value == null) {
                throw new IllegalArgumentException(String.format("Required key `%s` is not set", key));
            }
            return value;
        }

        private <T> T getOrDefault(String key, T defaultValue, Function<String, T> mapper) {
            var value = options.get(key);
            if (value == null) {
                return defaultValue;
            }
            return mapper.apply(value);
        }
    }
}
