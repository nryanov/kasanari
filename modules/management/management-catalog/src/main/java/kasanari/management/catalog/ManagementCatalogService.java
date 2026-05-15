package kasanari.management.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.repository.management.catalog.CatalogMetadataRepository;
import kasanari.repository.management.catalog.model.CatalogMetadata;
import kasanari.repository.management.catalog.model.CatalogSpec;
import kasanari.repository.management.catalog.postgres.JdbcCatalogMetadataRepository;
import kasanari.repository.management.catalog.postgres.JdbcManagementCatalogQueries;
import org.jdbi.v3.core.Handle;

import java.util.Optional;

public class ManagementCatalogService {
    private final TransactionManager<Handle> txManager;
    private final CatalogMetadataRepository<Handle> catalogRepository;

    public ManagementCatalogService(KasanariDataSource dataSource, ObjectMapper objectMapper) {
        this.txManager = new JdbcTransactionManager(dataSource);
        this.catalogRepository = new JdbcCatalogMetadataRepository(objectMapper);
        initSchema();
    }

    private void initSchema() {
        txManager.inTransaction(tx -> tx.createUpdate(JdbcManagementCatalogQueries.CREATE_CATALOG_REGISTRY_DDL).execute());
    }

    public boolean create(CatalogMetadata metadata) {
        return txManager.inTransactionR(tx -> catalogRepository.create(tx, metadata));
    }

    public boolean delete(String catalogId) {
        return txManager.inTransactionR(tx -> catalogRepository.delete(tx, catalogId));
    }

    public Optional<CatalogMetadata> get(String catalogId) {
        return txManager.inTransactionR(tx -> catalogRepository.getById(tx, catalogId));
    }

    public Optional<CatalogMetadata> update(String catalogId, CatalogSpec spec, Long expectedVersion) {
        return txManager.inTransactionR(tx -> catalogRepository.update(tx, catalogId, spec, expectedVersion));
    }
}
