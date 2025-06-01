package kasanari.catalog.iceberg.kasanari.repository.jdbc;

import kasanari.catalog.iceberg.kasanari.repository.CatalogRepository;

public class JdbcCatalogRepository implements CatalogRepository {
    private final KasanariDataSource dataSource;
    private final String catalogName;

    public JdbcCatalogRepository(KasanariDataSource dataSource, String catalogName) {
        this.dataSource = dataSource;
        this.catalogName = catalogName;
    }

    @Override
    public void register() {
        dataSource.getJdbi().useTransaction(tx -> {
            var registerCatalogQuery = tx.createUpdate(JdbcQueries.REGISTER_CATALOG);
            registerCatalogQuery.bind(0, catalogName);
            registerCatalogQuery.execute();
        });
    }

    @Override
    public boolean exists() {
        return dataSource.getJdbi().inTransaction(tx -> {
            var checkExistenceQuery = tx.createQuery(JdbcQueries.CHECK_IF_CATALOG_EXISTS);
            checkExistenceQuery.bind(0, catalogName);

            return checkExistenceQuery.mapTo(Boolean.class).first();
        });
    }
}
