package kasanari.repository.management.catalog.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.RepositoryBackend;
import kasanari.repository.management.catalog.CatalogMetadataRepository;
import kasanari.repository.management.catalog.ManagementCatalogRepositoryFactory;
import org.jdbi.v3.core.Handle;

public final class PostgresManagementCatalogRepositoryFactory implements ManagementCatalogRepositoryFactory {
    @Override
    public RepositoryBackend backend() {
        return RepositoryBackend.POSTGRES;
    }

    @Override
    public CatalogMetadataRepository<Handle> createRepository(ObjectMapper objectMapper) {
        return new JdbcCatalogMetadataRepository(objectMapper);
    }

    @Override
    public void initSchema(TransactionManager<Handle> txManager) {
        txManager.inTransaction(tx -> tx.createUpdate(JdbcManagementCatalogQueries.CREATE_CATALOG_REGISTRY_DDL).execute());
    }
}
