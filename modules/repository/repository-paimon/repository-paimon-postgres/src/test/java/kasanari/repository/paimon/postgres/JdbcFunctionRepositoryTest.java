package kasanari.repository.paimon.postgres;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.paimon.model.FunctionRecord;
import org.apache.paimon.catalog.Identifier;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static kasanari.repository.paimon.postgres.JdbcPaimonPostgresTestHelper.DEFAULT_CATALOG_KEY;
import static kasanari.repository.paimon.postgres.JdbcPaimonPostgresTestHelper.DEFAULT_DATABASE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcFunctionRepositoryTest {

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
        JdbcPaimonPostgresTestHelper.close();
        POSTGRES.stop();
    }

    @BeforeEach
    void beforeEach() {
        txManager = JdbcPaimonPostgresTestHelper.transactionManager(POSTGRES);
        JdbcPaimonPostgresTestHelper.initializeSchema(txManager);
        JdbcPaimonPostgresTestHelper.createDatabase(txManager, DEFAULT_CATALOG_KEY);
    }

    @AfterEach
    void afterEach() {
        JdbcPaimonPostgresTestHelper.truncateAll(postgresHelper);
    }

    @Test
    void createThenFindReturnsRecord() {
        var function = Identifier.create(DEFAULT_DATABASE, "fn");
        var record = JdbcPaimonPostgresTestHelper.functionRecord(DEFAULT_DATABASE, "fn");
        var repository = new JdbcFunctionRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.create(tx, record));

        var result = txManager.inTransactionR(tx -> repository.find(tx, function));

        var expected = Optional.of(record);
        assertEquals(expected, result);
    }

    @Test
    void findMissingReturnsEmpty() {
        var function = Identifier.create(DEFAULT_DATABASE, "missing");
        var repository = new JdbcFunctionRepository(DEFAULT_CATALOG_KEY);

        var result = txManager.inTransactionR(tx -> repository.find(tx, function));

        var expected = Optional.empty();
        assertEquals(expected, result);
    }

    @Test
    void alterUpdatesDefinition() {
        var function = Identifier.create(DEFAULT_DATABASE, "fn");
        var record = JdbcPaimonPostgresTestHelper.functionRecord(DEFAULT_DATABASE, "fn");
        var updated = new FunctionRecord(
                DEFAULT_DATABASE,
                "fn",
                false,
                JdbcPaimonPostgresTestHelper.sqlDefinition("SELECT 2"),
                Optional.of("updated comment"),
                Map.of("owner", "updated"));
        var repository = new JdbcFunctionRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.create(tx, record));
        txManager.inTransaction(tx -> repository.alter(tx, updated));

        var result = txManager.inTransactionR(tx -> repository.find(tx, function));

        var expected = Optional.of(updated);
        assertEquals(expected, result);
    }

    @Test
    void findAllReturnsCreatedFunctions() {
        var recordA = JdbcPaimonPostgresTestHelper.functionRecord(DEFAULT_DATABASE, "a");
        var recordB = JdbcPaimonPostgresTestHelper.functionRecord(DEFAULT_DATABASE, "b");
        var repository = new JdbcFunctionRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> {
            repository.create(tx, recordA);
            repository.create(tx, recordB);
        });

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, DEFAULT_DATABASE));

        var expected = List.of(recordA, recordB);
        assertEquals(expected, result);
    }

    @Test
    void deleteExistingReturnsTrue() {
        var function = Identifier.create(DEFAULT_DATABASE, "fn");
        var record = JdbcPaimonPostgresTestHelper.functionRecord(DEFAULT_DATABASE, "fn");
        var repository = new JdbcFunctionRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.create(tx, record));

        var deleteResult = txManager.inTransactionR(tx -> repository.delete(tx, function));
        var findResult = txManager.inTransactionR(tx -> repository.find(tx, function));

        var expectedDelete = true;
        var expectedFind = Optional.empty();
        assertEquals(expectedDelete, deleteResult);
        assertEquals(expectedFind, findResult);
    }

    @Test
    void deleteMissingReturnsFalse() {
        var function = Identifier.create(DEFAULT_DATABASE, "missing");
        var repository = new JdbcFunctionRepository(DEFAULT_CATALOG_KEY);

        var result = txManager.inTransactionR(tx -> repository.delete(tx, function));

        var expected = false;
        assertEquals(expected, result);
    }
}
