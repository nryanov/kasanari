package kasanari.repository.iceberg.postgres;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
import kasanari.repository.core.TransactionManager;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static kasanari.repository.iceberg.postgres.JdbcIcebergPostgresTestHelper.DEFAULT_CATALOG;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcCatalogRepositoryTest {

    private static final PostgresFixtureContainer POSTGRES = new PostgresFixtureContainer();
    private static PostgresHelper postgresHelper;
    private static TransactionManager<Handle> txManager;

    @BeforeAll
    static void setup() {
        POSTGRES.start();
        postgresHelper = new PostgresHelper(POSTGRES);
    }

    @AfterAll
    static void cleanup() {
        JdbcIcebergPostgresTestHelper.close();
        POSTGRES.stop();
    }

    @BeforeEach
    void beforeEach() {
        txManager = JdbcIcebergPostgresTestHelper.transactionManager(POSTGRES);
        JdbcIcebergPostgresTestHelper.initializeSchema(txManager);
    }

    @AfterEach
    void afterEach() {
        JdbcIcebergPostgresTestHelper.truncateAll(postgresHelper);
    }

    @Test
    void existsBeforeRegisterReturnsFalse() {
        var repository = new JdbcCatalogRepository(DEFAULT_CATALOG);

        var result = txManager.inTransactionR(repository::exists);

        var expected = false;
        assertEquals(expected, result);
    }

    @Test
    void registerThenExistsReturnsTrue() {
        JdbcIcebergPostgresTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var repository = new JdbcCatalogRepository(DEFAULT_CATALOG);

        var result = txManager.inTransactionR(repository::exists);

        var expected = true;
        assertEquals(expected, result);
    }
}
