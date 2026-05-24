package kasanari.repository.iceberg.postgres;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.repository.jdbc.KasanariDataSourceConfiguration;
import org.apache.iceberg.catalog.Namespace;
import org.jdbi.v3.core.Handle;

import java.util.Map;

public final class JdbcIcebergPostgresTestHelper {

    public static final String DEFAULT_CATALOG = "test_catalog";

    private static KasanariDataSource dataSource;
    private static TransactionManager<Handle> transactionManager;

    private JdbcIcebergPostgresTestHelper() {
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
        txManager.inTransaction(tx -> {
            tx.createUpdate(JdbcQueries.CREATE_CATALOGS_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_NAMESPACES_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_NAMESPACE_PROPERTIES_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_TABLES_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_VIEWS_DDL).execute();
        });
    }

    public static void truncateAll(PostgresHelper postgresHelper) {
        postgresHelper.truncateTable("kasanari_iceberg_namespace_properties");
        postgresHelper.truncateTable("kasanari_iceberg_tables");
        postgresHelper.truncateTable("kasanari_iceberg_views");
        postgresHelper.truncateTable("kasanari_iceberg_namespaces");
        postgresHelper.truncateTable("kasanari_iceberg_catalog");
    }

    public static void registerCatalog(TransactionManager<Handle> txManager, String catalogName) {
        var repository = new JdbcCatalogRepository(catalogName);
        txManager.inTransaction(repository::register);
    }

    public static void createNamespace(
            TransactionManager<Handle> txManager,
            String catalogName,
            Namespace namespace,
            Map<String, String> metadata) {
        var repository = new JdbcNamespaceRepository(catalogName);
        txManager.inTransaction(tx -> repository.create(tx, namespace, metadata));
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
            transactionManager = null;
        }
    }
}
