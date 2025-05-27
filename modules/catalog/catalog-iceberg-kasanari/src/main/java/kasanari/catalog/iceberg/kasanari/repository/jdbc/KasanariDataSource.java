package kasanari.catalog.iceberg.kasanari.repository.jdbc;


import io.agroal.api.configuration.AgroalConnectionFactoryConfiguration;
import io.agroal.api.configuration.supplier.AgroalConnectionFactoryConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalConnectionPoolConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.pool.DataSource;
import org.jdbi.v3.core.Jdbi;

public class KasanariDataSource {
    public void KasanariDataSource() {
        var agroalConnectionFactoryConfiguration = new AgroalConnectionFactoryConfigurationSupplier();
        // todo: configure
        agroalConnectionFactoryConfiguration.jdbcUrl("");
        agroalConnectionFactoryConfiguration.principal(new NamePrincipal(""));
        agroalConnectionFactoryConfiguration.credential("");
        agroalConnectionFactoryConfiguration.jdbcTransactionIsolation(AgroalConnectionFactoryConfiguration.TransactionIsolation.READ_COMMITTED);

        var agroalConnectionPoolConfiguration = new AgroalConnectionPoolConfigurationSupplier();
        // todo: configure
        agroalConnectionPoolConfiguration.initialSize(2);
        agroalConnectionPoolConfiguration.maxSize(5);
        agroalConnectionPoolConfiguration.connectionFactoryConfiguration(agroalConnectionFactoryConfiguration);

        var agroalConfiguration = new AgroalDataSourceConfigurationSupplier();
        agroalConfiguration.connectionPoolConfiguration(agroalConnectionPoolConfiguration);
        agroalConfiguration.metricsEnabled(false);

        var agroal = new DataSource(agroalConfiguration.get());
        var jdbi = Jdbi.create(agroal);
    }
}
