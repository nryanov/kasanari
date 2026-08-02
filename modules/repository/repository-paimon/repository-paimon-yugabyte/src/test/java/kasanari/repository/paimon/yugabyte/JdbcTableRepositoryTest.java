package kasanari.repository.paimon.yugabyte;

import kasanari.fixtures.yugabyte.YugabyteFixtureContainer;
import kasanari.fixtures.yugabyte.YugabyteHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.paimon.model.TableRecord;
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

import static kasanari.repository.paimon.yugabyte.JdbcPaimonYugabyteTestHelper.DEFAULT_CATALOG_KEY;
import static kasanari.repository.paimon.yugabyte.JdbcPaimonYugabyteTestHelper.DEFAULT_DATABASE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcTableRepositoryTest {

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
        JdbcPaimonYugabyteTestHelper.close();
        YUGABYTE.stop();
    }

    @BeforeEach
    void beforeEach() {
        txManager = JdbcPaimonYugabyteTestHelper.transactionManager(YUGABYTE);
        JdbcPaimonYugabyteTestHelper.initializeSchema(txManager);
        JdbcPaimonYugabyteTestHelper.createDatabase(txManager, DEFAULT_CATALOG_KEY);
    }

    @AfterEach
    void afterEach() {
        JdbcPaimonYugabyteTestHelper.truncateAll(yugabyteHelper);
    }

    @Test
    void createThenExistsReturnsTrue() {
        var table = Identifier.create(DEFAULT_DATABASE, "table");
        var record = JdbcPaimonYugabyteTestHelper.tableRecord(DEFAULT_DATABASE, "table");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.create(tx, record));

        var result = txManager.inTransactionR(tx -> repository.exists(tx, table));

        var expected = true;
        assertEquals(expected, result);
    }

    @Test
    void createThenFindReturnsRecord() {
        var table = Identifier.create(DEFAULT_DATABASE, "table");
        var record = JdbcPaimonYugabyteTestHelper.tableRecord(DEFAULT_DATABASE, "table");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.create(tx, record));

        var result = txManager.inTransactionR(tx -> repository.find(tx, table));

        var expected = Optional.of(record);
        assertEquals(expected, result);
    }

    @Test
    void findMissingReturnsEmpty() {
        var table = Identifier.create(DEFAULT_DATABASE, "missing");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG_KEY);

        var result = txManager.inTransactionR(tx -> repository.find(tx, table));

        var expected = Optional.empty();
        assertEquals(expected, result);
    }

    @Test
    void alterUpdatesProperties() {
        var table = Identifier.create(DEFAULT_DATABASE, "table");
        var record = JdbcPaimonYugabyteTestHelper.tableRecord(DEFAULT_DATABASE, "table");
        var updated = new TableRecord(DEFAULT_DATABASE, "table", Map.of("format", "orc"), record.tableUuid());
        var repository = new JdbcTableRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.create(tx, record));
        txManager.inTransaction(tx -> repository.alter(tx, updated));

        var result = txManager.inTransactionR(tx -> repository.find(tx, table));

        var expected = Optional.of(updated);
        assertEquals(expected, result);
    }

    @Test
    void findAllReturnsCreatedTables() {
        var recordA = JdbcPaimonYugabyteTestHelper.tableRecord(DEFAULT_DATABASE, "a");
        var recordB = JdbcPaimonYugabyteTestHelper.tableRecord(DEFAULT_DATABASE, "b");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG_KEY);
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
        var table = Identifier.create(DEFAULT_DATABASE, "table");
        var record = JdbcPaimonYugabyteTestHelper.tableRecord(DEFAULT_DATABASE, "table");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.create(tx, record));

        var deleteResult = txManager.inTransactionR(tx -> repository.delete(tx, table));
        var existsResult = txManager.inTransactionR(tx -> repository.exists(tx, table));

        var expectedDelete = true;
        var expectedExists = false;
        assertEquals(expectedDelete, deleteResult);
        assertEquals(expectedExists, existsResult);
    }

    @Test
    void deleteMissingReturnsFalse() {
        var table = Identifier.create(DEFAULT_DATABASE, "missing");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG_KEY);

        var result = txManager.inTransactionR(tx -> repository.delete(tx, table));

        var expected = false;
        assertEquals(expected, result);
    }

    @Test
    void renameExistingMovesTable() {
        var from = Identifier.create(DEFAULT_DATABASE, "from");
        var to = Identifier.create(DEFAULT_DATABASE, "to");
        var record = JdbcPaimonYugabyteTestHelper.tableRecord(DEFAULT_DATABASE, "from");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.create(tx, record));
        txManager.inTransaction(tx -> repository.rename(tx, from, to));

        var existsResult = txManager.inTransactionR(tx -> repository.exists(tx, to));
        var findResult = txManager.inTransactionR(tx -> repository.find(tx, to));

        var expectedExists = true;
        // uuid will not be changed
        var expectedRecord = new TableRecord(DEFAULT_DATABASE, "to", Map.of("format", "parquet"), Optional.of("uuid-from"));

        var expectedRecordOptional = Optional.of(expectedRecord);
        assertEquals(expectedExists, existsResult);
        assertEquals(expectedRecordOptional, findResult);
    }

    @Test
    void findByUuidReturnsRecord() {
        var record = JdbcPaimonYugabyteTestHelper.tableRecord(DEFAULT_DATABASE, "table");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.create(tx, record));

        var result = txManager.inTransactionR(tx -> repository.findByUuid(tx, record.tableUuid().orElseThrow()));

        var expected = Optional.of(record);
        assertEquals(expected, result);
    }
}
