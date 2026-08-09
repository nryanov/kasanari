package kasanari.repository.lance.postgres;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.repository.jdbc.KasanariDataSourceConfiguration;
import org.jdbi.v3.core.Handle;

import java.util.Map;

public final class JdbcLancePostgresTestHelper {
    public static final String DEFAULT_CATALOG_NAME = "lance_catalog_a";
    public static final String OTHER_CATALOG_NAME = "lance_catalog_b";

    private static KasanariDataSource dataSource;
    private static TransactionManager<Handle> transactionManager;

    private JdbcLancePostgresTestHelper() {
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
        new JdbcTableInitializer(txManager).initialize();
    }

    public static void truncateAll(PostgresHelper helper) {
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
