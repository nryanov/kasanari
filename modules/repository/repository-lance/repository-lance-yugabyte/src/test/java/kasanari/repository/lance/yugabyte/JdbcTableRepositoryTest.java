package kasanari.repository.lance.yugabyte;

import kasanari.fixtures.yugabyte.YugabyteFixtureContainer;
import kasanari.fixtures.yugabyte.YugabyteHelper;
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

import static kasanari.repository.lance.yugabyte.JdbcLanceYugabyteTestHelper.DEFAULT_CATALOG_KEY;
import static kasanari.repository.lance.yugabyte.JdbcLanceYugabyteTestHelper.OTHER_CATALOG_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdbcTableRepositoryTest {
    private static final YugabyteFixtureContainer YUGABYTE = new YugabyteFixtureContainer();
    private static YugabyteHelper helper;
    private static TransactionManager<Handle> txManager;

    @BeforeAll
    static void setup() {
        YUGABYTE.start();
        helper = new YugabyteHelper(YUGABYTE);
    }

    @AfterAll
    static void cleanup() {
        JdbcLanceYugabyteTestHelper.close();
        YUGABYTE.stop();
    }

    @BeforeEach
    void beforeEach() {
        txManager = JdbcLanceYugabyteTestHelper.transactionManager(YUGABYTE);
        JdbcLanceYugabyteTestHelper.initializeSchema(txManager);
        var namespaces = new JdbcNamespaceRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> namespaces.upsert(tx, "ns", Map.of()));
    }

    @AfterEach
    void afterEach() {
        JdbcLanceYugabyteTestHelper.truncateAll(helper);
    }

    @Test
    void upsertGetAndDeleteAreScopedByCatalogKey() {
        var tablesA = new JdbcTableRepository(DEFAULT_CATALOG_KEY);
        var tablesB = new JdbcTableRepository(OTHER_CATALOG_KEY);
        var namespacesB = new JdbcNamespaceRepository(OTHER_CATALOG_KEY);

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
