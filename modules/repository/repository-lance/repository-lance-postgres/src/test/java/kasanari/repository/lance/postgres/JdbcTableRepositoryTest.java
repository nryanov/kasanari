package kasanari.repository.lance.postgres;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.lance.model.TableMetadata;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static kasanari.repository.lance.postgres.JdbcLancePostgresTestHelper.DEFAULT_CATALOG_NAME;
import static kasanari.repository.lance.postgres.JdbcLancePostgresTestHelper.OTHER_CATALOG_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdbcTableRepositoryTest {
    private static final PostgresFixtureContainer POSTGRES = new PostgresFixtureContainer();
    private static PostgresHelper helper;
    private static TransactionManager<Handle> txManager;

    @BeforeAll
    static void setup() {
        POSTGRES.start();
        helper = new PostgresHelper(POSTGRES);
    }

    @AfterAll
    static void cleanup() {
        JdbcLancePostgresTestHelper.close();
        POSTGRES.stop();
    }

    @BeforeEach
    void beforeEach() {
        txManager = JdbcLancePostgresTestHelper.transactionManager(POSTGRES);
        JdbcLancePostgresTestHelper.initializeSchema(txManager);
        var namespaces = new JdbcNamespaceRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> namespaces.upsert(tx, "ns", Map.of()));
    }

    @AfterEach
    void afterEach() {
        JdbcLancePostgresTestHelper.truncateAll(helper);
    }

    @Test
    void upsertGetAndDeleteAreScopedByCatalogName() {
        var tablesA = new JdbcTableRepository(DEFAULT_CATALOG_NAME);
        var tablesB = new JdbcTableRepository(OTHER_CATALOG_NAME);
        var namespacesB = new JdbcNamespaceRepository(OTHER_CATALOG_NAME);

        txManager.inTransaction(tx -> {
            namespacesB.upsert(tx, "ns", Map.of());
            tablesA.upsert(tx, "ns.t1", "ns", "t1", "s3://wh/t1", Map.of("a", "1"));
        });

        boolean existsInA = txManager.inTransactionR(tx -> tablesA.exists(tx, "ns.t1"));
        boolean existsInB = txManager.inTransactionR(tx -> tablesB.exists(tx, "ns.t1"));
        assertTrue(existsInA);
        assertFalse(existsInB);

        Optional<TableMetadata> metadata = txManager.inTransactionR(tx -> tablesA.get(tx, "ns.t1"));
        assertTrue(metadata.isPresent());
        assertEquals("ns.t1", metadata.get().tableId());
        assertEquals("s3://wh/t1", metadata.get().location());

        boolean deleted = txManager.inTransactionR(tx -> tablesA.delete(tx, "ns.t1"));
        boolean existsAfterDelete = txManager.inTransactionR(tx -> tablesA.exists(tx, "ns.t1"));
        assertTrue(deleted);
        assertFalse(existsAfterDelete);
    }
}
