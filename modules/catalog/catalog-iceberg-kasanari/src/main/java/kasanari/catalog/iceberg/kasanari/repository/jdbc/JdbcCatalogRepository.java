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
    public void registerCurrentCatalog() {
        dataSource.getJdbi().useTransaction(tx -> {
            var checkExistenceQuery = tx.createQuery(JdbcQueries.CHECK_IF_CATALOG_EXISTS);
            checkExistenceQuery.bind(0, catalogName);

            var exists = checkExistenceQuery.mapTo(Boolean.class);

            if (!exists.first()) {
                var registerCatalogQuery = tx.createUpdate(JdbcQueries.REGISTER_CATALOG);
                registerCatalogQuery.bind(0, catalogName);
                registerCatalogQuery.execute();
            }
        });
    }
}
