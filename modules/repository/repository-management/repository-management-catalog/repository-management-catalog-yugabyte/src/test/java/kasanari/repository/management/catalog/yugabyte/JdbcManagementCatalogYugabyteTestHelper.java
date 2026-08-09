package kasanari.repository.management.catalog.yugabyte;

import kasanari.fixtures.yugabyte.YugabyteFixtureContainer;
import kasanari.fixtures.yugabyte.YugabyteHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.repository.management.catalog.model.CatalogSpec;
import org.jdbi.v3.core.Handle;

import java.util.HashMap;
import java.util.Map;

public final class JdbcManagementCatalogYugabyteTestHelper {

    private static KasanariDataSource dataSource;
    private static TransactionManager<Handle> transactionManager;

    private JdbcManagementCatalogYugabyteTestHelper() {
    }

    public static TransactionManager<Handle> transactionManager(YugabyteFixtureContainer yugabyte) {
        if (transactionManager == null) {
            dataSource = new KasanariDataSource(yugabyte.dataSourceProperties());
            transactionManager = new JdbcTransactionManager(dataSource);
        }
        return transactionManager;
    }

    public static void initializeSchema(TransactionManager<Handle> txManager) {
        txManager.inTransaction(tx ->
                tx.createUpdate(JdbcManagementCatalogQueries.CREATE_CATALOG_REGISTRY_DDL).execute());
    }

    public static void truncateAll(YugabyteHelper yugabyteHelper) {
        yugabyteHelper.truncateTable("kasanari_catalogs");
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
