package kasanari.repository.iceberg.postgres;


import kasanari.repository.iceberg.CatalogRepository;
import org.jdbi.v3.core.Handle;

public class JdbcCatalogRepository implements CatalogRepository<Handle> {
    private final String catalogName;

    public JdbcCatalogRepository(String catalogName) {
        this.catalogName = catalogName;
    }

    @Override
    public void register(Handle tx) {
        var registerCatalogQuery = tx.createUpdate(JdbcQueries.REGISTER_CATALOG);
        registerCatalogQuery.bind(0, catalogName);
        registerCatalogQuery.execute();
    }

    @Override
    public boolean exists(Handle tx) {
        var checkExistenceQuery = tx.createQuery(JdbcQueries.CHECK_IF_CATALOG_EXISTS);
        checkExistenceQuery.bind(0, catalogName);

        return checkExistenceQuery.mapTo(Boolean.class).first();
    }
}
