package kasanari.repository.iceberg.yugabyte;

import kasanari.repository.jdbc.KasanariDataSource;

public class JdbcTableInitializer {
    private final KasanariDataSource dataSource;

    public JdbcTableInitializer(KasanariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void initialize() {
        createTables();
    }

    private void createTables() {
        dataSource.getJdbi().useTransaction(tx -> {
            var catalogsDdlQuery = tx.createUpdate(JdbcQueries.CREATE_CATALOGS_DDL);
            var namespacesDdlQuery = tx.createUpdate(JdbcQueries.CREATE_NAMESPACES_DDL);
            var namespacePropertiesDdlQuery = tx.createUpdate(JdbcQueries.CREATE_NAMESPACE_PROPERTIES_DDL);
            var tablesDdlQuery = tx.createUpdate(JdbcQueries.CREATE_TABLES_DDL);
            var viewsDdlQuery = tx.createUpdate(JdbcQueries.CREATE_VIEWS_DDL);

            catalogsDdlQuery.execute();
            namespacesDdlQuery.execute();
            namespacePropertiesDdlQuery.execute();
            tablesDdlQuery.execute();
            viewsDdlQuery.execute();
        });
    }
}
