package kasanari.repository.paimon.postgres;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.paimon.model.ViewRecord;
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

import static kasanari.repository.paimon.postgres.JdbcPaimonPostgresTestHelper.DEFAULT_CATALOG_NAME;
import static kasanari.repository.paimon.postgres.JdbcPaimonPostgresTestHelper.DEFAULT_DATABASE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcViewRepositoryTest {

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
        JdbcPaimonPostgresTestHelper.createDatabase(txManager, DEFAULT_CATALOG_NAME);
    }

    @AfterEach
    void afterEach() {
        JdbcPaimonPostgresTestHelper.truncateAll(postgresHelper);
    }

    @Test
    void createThenFindReturnsRecord() {
        var view = Identifier.create(DEFAULT_DATABASE, "view");
        var record = JdbcPaimonPostgresTestHelper.viewRecord(DEFAULT_DATABASE, "view");
        var repository = new JdbcViewRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.create(tx, record));

        var result = txManager.inTransactionR(tx -> repository.find(tx, view));

        var expected = Optional.of(record);
        assertEquals(expected, result);
    }

    @Test
    void findMissingReturnsEmpty() {
        var view = Identifier.create(DEFAULT_DATABASE, "missing");
        var repository = new JdbcViewRepository(DEFAULT_CATALOG_NAME);

        var result = txManager.inTransactionR(tx -> repository.find(tx, view));

        var expected = Optional.empty();
        assertEquals(expected, result);
    }

    @Test
    void alterUpdatesQuery() {
        var view = Identifier.create(DEFAULT_DATABASE, "view");
        var record = JdbcPaimonPostgresTestHelper.viewRecord(DEFAULT_DATABASE, "view");
        var updated = new ViewRecord(
                DEFAULT_DATABASE,
                "view",
                "SELECT 2",
                record.dialects(),
                record.options(),
                record.comment());
        var repository = new JdbcViewRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.create(tx, record));
        txManager.inTransaction(tx -> repository.alter(tx, updated));

        var result = txManager.inTransactionR(tx -> repository.find(tx, view));

        var expected = Optional.of(updated);
        assertEquals(expected, result);
    }

    @Test
    void findAllReturnsCreatedViews() {
        var recordA = JdbcPaimonPostgresTestHelper.viewRecord(DEFAULT_DATABASE, "a");
        var recordB = JdbcPaimonPostgresTestHelper.viewRecord(DEFAULT_DATABASE, "b");
        var repository = new JdbcViewRepository(DEFAULT_CATALOG_NAME);
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
        var view = Identifier.create(DEFAULT_DATABASE, "view");
        var record = JdbcPaimonPostgresTestHelper.viewRecord(DEFAULT_DATABASE, "view");
        var repository = new JdbcViewRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.create(tx, record));

        var deleteResult = txManager.inTransactionR(tx -> repository.delete(tx, view));
        var findResult = txManager.inTransactionR(tx -> repository.find(tx, view));

        var expectedDelete = true;
        var expectedFind = Optional.empty();
        assertEquals(expectedDelete, deleteResult);
        assertEquals(expectedFind, findResult);
    }

    @Test
    void renameExistingReturnsTrue() {
        var from = Identifier.create(DEFAULT_DATABASE, "from");
        var to = Identifier.create(DEFAULT_DATABASE, "to");
        var record = JdbcPaimonPostgresTestHelper.viewRecord(DEFAULT_DATABASE, "from");
        var expectedRecord = JdbcPaimonPostgresTestHelper.viewRecord(DEFAULT_DATABASE, "to");
        var repository = new JdbcViewRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.create(tx, record));

        var renameResult = txManager.inTransactionR(tx -> repository.rename(tx, from, to));
        var findResult = txManager.inTransactionR(tx -> repository.find(tx, to));

        var expectedRename = true;
        var expectedFind = Optional.of(expectedRecord);
        assertEquals(expectedRename, renameResult);
        assertEquals(expectedFind, findResult);
    }
}
