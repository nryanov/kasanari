package kasanari.repository.jdbc;


import io.agroal.api.configuration.AgroalConnectionFactoryConfiguration;
import io.agroal.api.configuration.supplier.AgroalConnectionFactoryConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalConnectionPoolConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.agroal.pool.DataSource;
import org.jdbi.v3.core.Jdbi;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

public class KasanariDataSource implements AutoCloseable {
    private final DataSource pool;
    private final Jdbi jdbi;
    private final RepositoryBackend repositoryBackend;

    public KasanariDataSource(Map<String, String> properties) {
        this.repositoryBackend = RepositoryBackend.from(properties);
        var dataSourceProperties = new DataSourceProperties(properties);
        var agroalConnectionFactoryConfiguration = new AgroalConnectionFactoryConfigurationSupplier();

        agroalConnectionFactoryConfiguration.jdbcUrl(dataSourceProperties.getUri());
        agroalConnectionFactoryConfiguration.principal(new NamePrincipal(dataSourceProperties.getUser()));
        agroalConnectionFactoryConfiguration.credential(new SimplePassword(dataSourceProperties.getPassword()));
        agroalConnectionFactoryConfiguration.jdbcTransactionIsolation(AgroalConnectionFactoryConfiguration.TransactionIsolation.READ_COMMITTED);

        var agroalConnectionPoolConfiguration = new AgroalConnectionPoolConfigurationSupplier();

        agroalConnectionPoolConfiguration.initialSize(dataSourceProperties.getConnectionPoolInitialSize());
        agroalConnectionPoolConfiguration.minSize(dataSourceProperties.getConnectionPoolMinSize());
        agroalConnectionPoolConfiguration.maxSize(dataSourceProperties.getConnectionPoolMaxSize());
        agroalConnectionPoolConfiguration.maxLifetime(dataSourceProperties.getConnectionMaxLifetimeMillis());
        agroalConnectionPoolConfiguration.connectionFactoryConfiguration(agroalConnectionFactoryConfiguration);

        var agroalConfiguration = new AgroalDataSourceConfigurationSupplier();
        agroalConfiguration.connectionPoolConfiguration(agroalConnectionPoolConfiguration);

        this.pool = new DataSource(agroalConfiguration.get());
        this.jdbi = Jdbi.create(pool);
    }

    public RepositoryBackend repositoryBackend() {
        return repositoryBackend;
    }

    private record DataSourceProperties(Map<String, String> properties) {
        public String getUser() {
            return getRequired(KasanariDataSourceConfiguration.USER);
        }

        public String getPassword() {
            return getRequired(KasanariDataSourceConfiguration.PASSWORD);
        }

        public String getUri() {
            return getRequired(KasanariDataSourceConfiguration.URI);
        }

        public int getConnectionPoolInitialSize() {
            return getOrDefault(
                    KasanariDataSourceConfiguration.CONNECTION_POOL_INITIAL_SIZE,
                    KasanariDataSourceConfiguration.CONNECTION_POOL_INITIAL_SIZE_DEFAULT,
                    Integer::parseInt
            );
        }

        public int getConnectionPoolMinSize() {
            return getOrDefault(
                    KasanariDataSourceConfiguration.CONNECTION_POOL_MIN_SIZE,
                    KasanariDataSourceConfiguration.CONNECTION_POOL_MIN_SIZE_DEFAULT,
                    Integer::parseInt
            );
        }

        public int getConnectionPoolMaxSize() {
            return getOrDefault(
                    KasanariDataSourceConfiguration.CONNECTION_POOL_MAX_SIZE,
                    KasanariDataSourceConfiguration.CONNECTION_POOL_MAX_SIZE_DEFAULT,
                    Integer::parseInt
            );
        }

        public Duration getConnectionMaxLifetimeMillis() {
            return getOrDefault(
                    KasanariDataSourceConfiguration.CONNECTION_MAX_LIFETIME_MILLIS,
                    KasanariDataSourceConfiguration.CONNECTION_MAX_LIFETIME_MILLIS_DEFAULT,
                    value -> Duration.ofMillis(Long.parseLong(value))
            );
        }

        private String getRequired(String key) {
            var maybeValue = properties.get(key);

            if (maybeValue == null) {
                throw new IllegalArgumentException(String.format("Required key `%s` is not set", key));
            }

            return maybeValue;
        }

        private <T> T getOrDefault(String key, T defaultValue, Function<String, T> mapper) {
            var maybeValue = properties.get(key);

            if (maybeValue == null) {
                return defaultValue;
            }

            return mapper.apply(maybeValue);
        }
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    @Override
    public void close() {
        pool.close();
    }
}
