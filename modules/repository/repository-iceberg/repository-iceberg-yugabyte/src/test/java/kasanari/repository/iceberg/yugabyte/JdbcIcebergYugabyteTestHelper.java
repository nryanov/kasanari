package kasanari.repository.iceberg.yugabyte;

import kasanari.fixtures.yugabyte.YugabyteFixtureContainer;
import kasanari.fixtures.yugabyte.YugabyteHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import org.apache.iceberg.catalog.Namespace;
import org.jdbi.v3.core.Handle;

import java.util.Map;

public final class JdbcIcebergYugabyteTestHelper {

    public static final String DEFAULT_CATALOG = "test_catalog";

    private static KasanariDataSource dataSource;
    private static TransactionManager<Handle> transactionManager;

    private JdbcIcebergYugabyteTestHelper() {
    }

    public static TransactionManager<Handle> transactionManager(YugabyteFixtureContainer yugabyte) {
        if (transactionManager == null) {
            dataSource = new KasanariDataSource(yugabyte.dataSourceProperties());
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

    public static void truncateAll(YugabyteHelper yugabyteHelper) {
        yugabyteHelper.truncateTable("kasanari_iceberg_namespace_properties");
        yugabyteHelper.truncateTable("kasanari_iceberg_tables");
        yugabyteHelper.truncateTable("kasanari_iceberg_views");
        yugabyteHelper.truncateTable("kasanari_iceberg_namespaces");
        yugabyteHelper.truncateTable("kasanari_iceberg_catalog");
    }

    public static void registerCatalog(TransactionManager<Handle> txManager, String catalogKey) {
        var repository = new JdbcCatalogRepository(catalogKey);
        txManager.inTransaction(repository::register);
    }

    public static void createNamespace(
            TransactionManager<Handle> txManager,
            String catalogKey,
            Namespace namespace,
            Map<String, String> metadata) {
        var repository = new JdbcNamespaceRepository(catalogKey);
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
