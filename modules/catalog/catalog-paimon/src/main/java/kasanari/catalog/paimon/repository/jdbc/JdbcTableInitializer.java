package kasanari.catalog.paimon.repository.jdbc;

public class JdbcTableInitializer {
    private final KasanariDataSource dataSource;

    public JdbcTableInitializer(KasanariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void initialize() {
        dataSource.getJdbi().useTransaction(tx -> {
            tx.createUpdate(JdbcQueries.CREATE_DATABASES_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_TABLES_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_VIEWS_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_FUNCTIONS_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_BRANCHES_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_TAGS_DDL).execute();
        });
    }
}
