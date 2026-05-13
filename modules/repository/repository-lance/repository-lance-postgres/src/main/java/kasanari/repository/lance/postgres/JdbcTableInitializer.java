package kasanari.repository.lance.postgres;

import kasanari.repository.jdbc.KasanariDataSource;

public class JdbcTableInitializer {
    private final KasanariDataSource dataSource;

    public JdbcTableInitializer(KasanariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void initialize() {
        dataSource.getJdbi().useTransaction(tx -> {
            tx.createUpdate(JdbcQueries.CREATE_NAMESPACES_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_TABLES_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_TABLE_VERSIONS_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_TRANSACTIONS_DDL).execute();
        });
    }
}
