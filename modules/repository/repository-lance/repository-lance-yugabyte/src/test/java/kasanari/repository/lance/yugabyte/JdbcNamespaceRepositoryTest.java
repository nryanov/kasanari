package kasanari.repository.lance.yugabyte;

import kasanari.fixtures.yugabyte.YugabyteFixtureContainer;
import kasanari.fixtures.yugabyte.YugabyteHelper;
import kasanari.repository.core.TransactionManager;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static kasanari.repository.lance.yugabyte.JdbcLanceYugabyteTestHelper.DEFAULT_CATALOG_KEY;
import static kasanari.repository.lance.yugabyte.JdbcLanceYugabyteTestHelper.OTHER_CATALOG_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdbcNamespaceRepositoryTest {
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
    }

    @AfterEach
    void afterEach() {
        JdbcLanceYugabyteTestHelper.truncateAll(helper);
    }

    @Test
    void upsertAndExistsAreScopedByCatalogKey() {
        var repoA = new JdbcNamespaceRepository(DEFAULT_CATALOG_KEY);
        var repoB = new JdbcNamespaceRepository(OTHER_CATALOG_KEY);

        txManager.inTransaction(tx -> repoA.upsert(tx, "ns", Map.of("k", "v")));

        boolean existsInA = txManager.inTransactionR(tx -> repoA.exists(tx, "ns"));
        boolean existsInB = txManager.inTransactionR(tx -> repoB.exists(tx, "ns"));
        assertTrue(existsInA);
        assertFalse(existsInB);
    }

    @Test
    void listReturnsOnlyCurrentCatalogNamespaces() {
        var repoA = new JdbcNamespaceRepository(DEFAULT_CATALOG_KEY);
        var repoB = new JdbcNamespaceRepository(OTHER_CATALOG_KEY);

        txManager.inTransaction(tx -> {
            repoA.upsert(tx, "alpha", Map.of());
            repoB.upsert(tx, "beta", Map.of());
        });

        List<String> listed = txManager.inTransactionR(tx -> repoA.list(tx, ""));
        assertEquals(1, listed.size());
        assertEquals("alpha", listed.getFirst());
    }
}
