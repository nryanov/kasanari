package kasanari.repository.lance.postgres;

import kasanari.repository.core.TransactionManager;
import org.jdbi.v3.core.Handle;

public class JdbcTableInitializer {
    private final TransactionManager<Handle> transactionManager;

    public JdbcTableInitializer(TransactionManager<Handle> transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void initialize() {
        transactionManager.inTransaction(tx -> {
            tx.createUpdate(JdbcQueries.CREATE_NAMESPACES_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_TABLES_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_TABLES_FK_INDEX).execute();
        });
    }
}
