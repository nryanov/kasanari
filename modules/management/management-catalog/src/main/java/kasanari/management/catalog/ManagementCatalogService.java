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
import kasanari.core.model.CatalogType;
import org.jdbi.v3.core.Handle;

import java.util.List;
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

    public boolean delete(CatalogType catalogType, String catalogName) {
        return txManager.inTransactionR(tx -> catalogRepository.delete(tx, catalogType, catalogName));
    }

    public List<CatalogMetadata> list(CatalogType catalogType) {
        return txManager.inTransactionR(tx -> catalogRepository.list(tx, catalogType));
    }

    public Optional<CatalogMetadata> get(CatalogType catalogType, String catalogName) {
        return txManager.inTransactionR(tx -> catalogRepository.getByName(tx, catalogType, catalogName));
    }

    public Optional<CatalogMetadata> update(CatalogType catalogType, String catalogName, CatalogSpec spec, Long expectedVersion) {
        return txManager.inTransactionR(tx -> catalogRepository.update(tx, catalogType, catalogName, spec, expectedVersion));
    }
}
