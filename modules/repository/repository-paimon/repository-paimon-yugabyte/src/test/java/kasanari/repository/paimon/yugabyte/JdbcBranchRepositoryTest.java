package kasanari.repository.paimon.yugabyte;

import kasanari.fixtures.yugabyte.YugabyteFixtureContainer;
import kasanari.fixtures.yugabyte.YugabyteHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.paimon.model.BranchRecord;
import org.apache.paimon.catalog.Identifier;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static kasanari.repository.paimon.yugabyte.JdbcPaimonYugabyteTestHelper.DEFAULT_CATALOG_NAME;
import static kasanari.repository.paimon.yugabyte.JdbcPaimonYugabyteTestHelper.DEFAULT_DATABASE;
import static kasanari.repository.paimon.yugabyte.JdbcPaimonYugabyteTestHelper.DEFAULT_TABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcBranchRepositoryTest {

    private static final YugabyteFixtureContainer YUGABYTE = new YugabyteFixtureContainer();
    private static YugabyteHelper yugabyteHelper;
    private static TransactionManager<Handle> txManager;

    private static final Identifier TABLE = Identifier.create(DEFAULT_DATABASE, DEFAULT_TABLE);

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
        JdbcPaimonYugabyteTestHelper.createDatabaseAndTable(txManager, DEFAULT_CATALOG_NAME);
    }

    @AfterEach
    void afterEach() {
        JdbcPaimonYugabyteTestHelper.truncateAll(yugabyteHelper);
    }

    @Test
    void createThenFindAllReturnsBranch() {
        var record = JdbcPaimonYugabyteTestHelper.branchRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "dev");
        var repository = new JdbcBranchRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.create(tx, record));

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE));

        var expected = List.of(record);
        assertEquals(expected, result);
    }

    @Test
    void deleteExistingReturnsTrue() {
        var record = JdbcPaimonYugabyteTestHelper.branchRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "dev");
        var repository = new JdbcBranchRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.create(tx, record));

        var deleteResult = txManager.inTransactionR(tx -> repository.delete(tx, TABLE, "dev"));
        var findResult = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE));

        var expectedDelete = true;
        var expectedFind = List.<BranchRecord>of();
        assertEquals(expectedDelete, deleteResult);
        assertEquals(expectedFind, findResult);
    }

    @Test
    void deleteMissingReturnsFalse() {
        var repository = new JdbcBranchRepository(DEFAULT_CATALOG_NAME);

        var result = txManager.inTransactionR(tx -> repository.delete(tx, TABLE, "missing"));

        var expected = false;
        assertEquals(expected, result);
    }

    @Test
    void renameExistingReturnsTrue() {
        var record = JdbcPaimonYugabyteTestHelper.branchRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "from");
        var expectedRecord = new BranchRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "to", record.tagName());
        var repository = new JdbcBranchRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.create(tx, record));

        var renameResult = txManager.inTransactionR(tx -> repository.rename(tx, TABLE, "from", "to"));
        var findResult = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE));

        var expectedRename = true;
        var expectedFind = List.of(expectedRecord);
        assertEquals(expectedRename, renameResult);
        assertEquals(expectedFind, findResult);
    }

    @Test
    void fastForwardClearsTagName() {
        var record = JdbcPaimonYugabyteTestHelper.branchRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "dev");
        var expectedRecord = new BranchRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "dev", Optional.empty());
        var repository = new JdbcBranchRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.create(tx, record));

        var fastForwardResult = txManager.inTransactionR(tx -> repository.fastForward(tx, TABLE, "dev"));
        var findResult = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE));

        var expectedFastForward = true;
        var expectedFind = List.of(expectedRecord);
        assertEquals(expectedFastForward, fastForwardResult);
        assertEquals(expectedFind, findResult);
    }
}
