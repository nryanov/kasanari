package kasanari.repository.lance.yugabyte;

import kasanari.fixtures.yugabyte.YugabyteFixtureContainer;
import kasanari.fixtures.yugabyte.YugabyteHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import org.jdbi.v3.core.Handle;

public final class JdbcLanceYugabyteTestHelper {
    public static final String DEFAULT_CATALOG_KEY = "lance_catalog_a";
    public static final String OTHER_CATALOG_KEY = "lance_catalog_b";

    private static KasanariDataSource dataSource;
    private static TransactionManager<Handle> transactionManager;

    private JdbcLanceYugabyteTestHelper() {
    }

    public static TransactionManager<Handle> transactionManager(YugabyteFixtureContainer yugabyte) {
        if (transactionManager == null) {
            dataSource = new KasanariDataSource(yugabyte.dataSourceProperties());
            transactionManager = new JdbcTransactionManager(dataSource);
        }
        return transactionManager;
    }

    public static void initializeSchema(TransactionManager<Handle> txManager) {
        new JdbcTableInitializer(txManager).initialize();
    }

    public static void truncateAll(YugabyteHelper helper) {
        helper.truncateTable("kasanari_lance_tables");
        helper.truncateTable("kasanari_lance_namespaces");
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
            transactionManager = null;
        }
    }
}
