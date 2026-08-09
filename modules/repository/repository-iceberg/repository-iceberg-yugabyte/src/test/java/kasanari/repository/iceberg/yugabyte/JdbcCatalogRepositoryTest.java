package kasanari.repository.iceberg.yugabyte;

import kasanari.fixtures.yugabyte.YugabyteFixtureContainer;
import kasanari.fixtures.yugabyte.YugabyteHelper;
import kasanari.repository.core.TransactionManager;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static kasanari.repository.iceberg.yugabyte.JdbcIcebergYugabyteTestHelper.DEFAULT_CATALOG;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcCatalogRepositoryTest {

    private static final YugabyteFixtureContainer YUGABYTE = new YugabyteFixtureContainer();
    private static YugabyteHelper yugabyteHelper;
    private static TransactionManager<Handle> txManager;

    @BeforeAll
    static void setup() {
        YUGABYTE.start();
        yugabyteHelper = new YugabyteHelper(YUGABYTE);
    }

    @AfterAll
    static void cleanup() {
        JdbcIcebergYugabyteTestHelper.close();
        YUGABYTE.stop();
    }

    @BeforeEach
    void beforeEach() {
        txManager = JdbcIcebergYugabyteTestHelper.transactionManager(YUGABYTE);
        JdbcIcebergYugabyteTestHelper.initializeSchema(txManager);
    }

    @AfterEach
    void afterEach() {
        JdbcIcebergYugabyteTestHelper.truncateAll(yugabyteHelper);
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
        JdbcIcebergYugabyteTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var repository = new JdbcCatalogRepository(DEFAULT_CATALOG);

        var result = txManager.inTransactionR(repository::exists);

        var expected = true;
        assertEquals(expected, result);
    }
}
