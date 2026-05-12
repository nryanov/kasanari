package kasanari.management.catalog;

import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.repository.management.CatalogMetadataRepository;
import kasanari.repository.management.postgres.JdbcCatalogMetadataRepository;
import kasanari.repository.management.postgres.JdbcManagementQueries;
import kasanari.repository.management.postgres.JdbcRoleBindingRepository;
import org.jdbi.v3.core.Handle;

public class ManagementCatalogService {
    private final KasanariDataSource dataSource;
    private final TransactionManager<Handle> txManager;
    private final CatalogMetadataRepository<Handle> catalogRepository;

    public ManagementCatalogService(ManagementMetadataConfiguration configuration, ObjectMapper objectMapper) {
        this.dataSource = new KasanariDataSource(configuration.jdbcProperties());
        this.txManager = new JdbcTransactionManager(dataSource);
        this.catalogRepository = new JdbcCatalogMetadataRepository(objectMapper);
        this.roleBindingRepository = new JdbcRoleBindingRepository();
        initSchema();
    }

    private void initSchema() {
        txManager.inTransaction(tx -> tx.createUpdate(JdbcManagementQueries.CREATE_CATALOG_REGISTRY_DDL).execute());
    }
}
