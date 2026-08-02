package kasanari.repository.management.security.yugabyte;

import kasanari.fixtures.yugabyte.YugabyteFixtureContainer;
import kasanari.fixtures.yugabyte.YugabyteHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import org.jdbi.v3.core.Handle;

public final class JdbcManagementSecurityYugabyteTestHelper {

    private final YugabyteHelper yugabyteHelper;
    private final KasanariDataSource dataSource;
    private final TransactionManager<Handle> transactionManager;

    public JdbcManagementSecurityYugabyteTestHelper(YugabyteFixtureContainer yugabyte) {
        this.yugabyteHelper = new YugabyteHelper(yugabyte);
        this.dataSource = new KasanariDataSource(yugabyte.dataSourceProperties());
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
        yugabyteHelper.truncateTable("kasanari_role_bindings");
        transactionManager.inTransaction(tx ->
                tx.createUpdate(JdbcManagementSecurityQueries.RESET_ROLE_BINDING_REVISION).execute());
    }

    public void close() {
        dataSource.close();
    }
}
