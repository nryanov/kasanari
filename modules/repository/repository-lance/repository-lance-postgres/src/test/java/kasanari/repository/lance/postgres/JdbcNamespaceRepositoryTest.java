package kasanari.repository.lance.postgres;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
import kasanari.repository.core.TransactionManager;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static kasanari.repository.lance.postgres.JdbcLancePostgresTestHelper.DEFAULT_CATALOG_NAME;
import static kasanari.repository.lance.postgres.JdbcLancePostgresTestHelper.OTHER_CATALOG_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdbcNamespaceRepositoryTest {
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
    }

    @AfterEach
    void afterEach() {
        JdbcLancePostgresTestHelper.truncateAll(helper);
    }

    @Test
    void upsertAndExistsAreScopedByCatalogName() {
        var repoA = new JdbcNamespaceRepository(DEFAULT_CATALOG_NAME);
        var repoB = new JdbcNamespaceRepository(OTHER_CATALOG_NAME);

        txManager.inTransaction(tx -> repoA.upsert(tx, "ns", Map.of("k", "v")));

        boolean existsInA = txManager.inTransactionR(tx -> repoA.exists(tx, "ns"));
        boolean existsInB = txManager.inTransactionR(tx -> repoB.exists(tx, "ns"));
        assertTrue(existsInA);
        assertFalse(existsInB);
    }

    @Test
    void listReturnsOnlyCurrentCatalogNamespaces() {
        var repoA = new JdbcNamespaceRepository(DEFAULT_CATALOG_NAME);
        var repoB = new JdbcNamespaceRepository(OTHER_CATALOG_NAME);

        txManager.inTransaction(tx -> {
            repoA.upsert(tx, "alpha", Map.of());
            repoB.upsert(tx, "beta", Map.of());
        });

        List<String> listed = txManager.inTransactionR(tx -> repoA.list(tx, ""));
        assertEquals(1, listed.size());
        assertEquals("alpha", listed.getFirst());
    }
}
