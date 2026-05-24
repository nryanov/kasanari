package kasanari.repository.management.catalog.postgres;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.repository.jdbc.KasanariDataSourceConfiguration;
import kasanari.repository.management.catalog.model.CatalogSpec;
import org.jdbi.v3.core.Handle;

import java.util.HashMap;
import java.util.Map;

public final class JdbcManagementCatalogPostgresTestHelper {

    private static KasanariDataSource dataSource;
    private static TransactionManager<Handle> transactionManager;

    private JdbcManagementCatalogPostgresTestHelper() {
    }

    public static TransactionManager<Handle> transactionManager(PostgresFixtureContainer postgres) {
        if (transactionManager == null) {
            dataSource = new KasanariDataSource(Map.of(
                    KasanariDataSourceConfiguration.URI, postgres.jdbcUrl(),
                    KasanariDataSourceConfiguration.USER, postgres.username(),
                    KasanariDataSourceConfiguration.PASSWORD, postgres.password()
            ));
            transactionManager = new JdbcTransactionManager(dataSource);
        }
        return transactionManager;
    }

    public static void initializeSchema(TransactionManager<Handle> txManager) {
        txManager.inTransaction(tx ->
                tx.createUpdate(JdbcManagementCatalogQueries.CREATE_CATALOG_REGISTRY_DDL).execute());
    }

    public static void truncateAll(PostgresHelper postgresHelper) {
        postgresHelper.truncateTable("kasanari_catalogs");
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
            transactionManager = null;
        }
    }

    public static CatalogSpec catalogSpec() {
        return new CatalogSpec(new HashMap<>(), new HashMap<>());
    }
}
