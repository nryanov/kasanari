package kasanari.repository.iceberg.yugabyte;


import kasanari.repository.iceberg.CatalogRepository;
import org.jdbi.v3.core.Handle;

public class JdbcCatalogRepository implements CatalogRepository<Handle> {
    private final String catalogKey;

    public JdbcCatalogRepository(String catalogKey) {
        this.catalogKey = catalogKey;
    }

    @Override
    public void register(Handle tx) {
        var registerCatalogQuery = tx.createUpdate(JdbcQueries.UPSERT_CATALOG);
        registerCatalogQuery.bind(0, catalogKey);
        registerCatalogQuery.execute();
    }

    @Override
    public boolean exists(Handle tx) {
        var checkExistenceQuery = tx.createQuery(JdbcQueries.CHECK_IF_CATALOG_EXISTS);
        checkExistenceQuery.bind(0, catalogKey);

        return checkExistenceQuery.mapTo(Boolean.class).first();
    }
}
