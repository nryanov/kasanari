package kasanari.repository.management.security.postgres;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.repository.jdbc.KasanariDataSourceConfiguration;
import org.jdbi.v3.core.Handle;

import java.util.Map;

public final class JdbcManagementSecurityPostgresTestHelper {

    private final PostgresHelper postgresHelper;
    private final KasanariDataSource dataSource;
    private final TransactionManager<Handle> transactionManager;

    public JdbcManagementSecurityPostgresTestHelper(PostgresFixtureContainer postgres) {
        this.postgresHelper = new PostgresHelper(postgres);
        this.dataSource = new KasanariDataSource(Map.of(
                KasanariDataSourceConfiguration.URI, postgres.jdbcUrl(),
                KasanariDataSourceConfiguration.USER, postgres.username(),
                KasanariDataSourceConfiguration.PASSWORD, postgres.password()
        ));
        this.transactionManager = new JdbcTransactionManager(dataSource);
    }

    public TransactionManager<Handle> transactionManager() {
        return transactionManager;
    }

    public void initializeSchema() {
        transactionManager.inTransaction(tx -> {
            tx.createUpdate(JdbcManagementSecurityQueries.CREATE_ROLE_BINDINGS_DDL).execute();
            tx.createUpdate(JdbcManagementSecurityQueries.CREATE_ROLE_BINDING_REVISION_DDL).execute();
            tx.createUpdate(JdbcManagementSecurityQueries.INSERT_ROLE_BINDING_REVISION).execute();
        });
    }

    public void truncateAll() {
        postgresHelper.truncateTable("kasanari_role_bindings");
        transactionManager.inTransaction(tx ->
                tx.createUpdate(JdbcManagementSecurityQueries.RESET_ROLE_BINDING_REVISION).execute());
    }

    public void close() {
        dataSource.close();
    }
}
