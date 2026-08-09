package kasanari.repository.management.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.BackendAwareFactory;
import org.jdbi.v3.core.Handle;

public interface ManagementCatalogRepositoryFactory extends BackendAwareFactory {
    CatalogMetadataRepository<Handle> createRepository(ObjectMapper objectMapper);

    void initSchema(TransactionManager<Handle> txManager);
}
