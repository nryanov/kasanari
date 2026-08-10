package kasanari.repository.paimon.yugabyte;

import kasanari.fixtures.yugabyte.YugabyteFixtureContainer;
import kasanari.fixtures.yugabyte.YugabyteHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.paimon.model.TagRecord;
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

public class JdbcTagRepositoryTest {

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
    void createThenFindReturnsRecord() {
        var record = JdbcPaimonYugabyteTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "v1");
        var repository = new JdbcTagRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.create(tx, record, false));

        var result = txManager.inTransactionR(tx -> repository.find(tx, TABLE, "v1"));

        var expected = Optional.of(record);
        assertEquals(expected, result);
    }

    @Test
    void createIgnoreIfExistsDoesNotFail() {
        var record = JdbcPaimonYugabyteTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "v1");
        var repository = new JdbcTagRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.create(tx, record, false));
        txManager.inTransaction(tx -> repository.create(tx, record, true));

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE, Optional.empty()));

        var expected = List.of("v1");
        assertEquals(expected, result);
    }

    @Test
    void existsReturnsTrueAfterCreate() {
        var record = JdbcPaimonYugabyteTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "v1");
        var repository = new JdbcTagRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.create(tx, record, false));

        var result = txManager.inTransactionR(tx -> repository.exists(tx, TABLE, "v1"));

        var expected = true;
        assertEquals(expected, result);
    }

    @Test
    void deleteExistingReturnsTrue() {
        var record = JdbcPaimonYugabyteTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "v1");
        var repository = new JdbcTagRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.create(tx, record, false));

        var deleteResult = txManager.inTransactionR(tx -> repository.delete(tx, TABLE, "v1"));
        var existsResult = txManager.inTransactionR(tx -> repository.exists(tx, TABLE, "v1"));

        var expectedDelete = true;
        var expectedExists = false;
        assertEquals(expectedDelete, deleteResult);
        assertEquals(expectedExists, existsResult);
    }

    @Test
    void deleteMissingReturnsFalse() {
        var repository = new JdbcTagRepository(DEFAULT_CATALOG_NAME);

        var result = txManager.inTransactionR(tx -> repository.delete(tx, TABLE, "missing"));

        var expected = false;
        assertEquals(expected, result);
    }

    @Test
    void findAllReturnsTagNames() {
        var recordA = JdbcPaimonYugabyteTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "a");
        var recordB = JdbcPaimonYugabyteTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "b");
        var repository = new JdbcTagRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> {
            repository.create(tx, recordA, false);
            repository.create(tx, recordB, false);
        });

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE, Optional.empty()));

        var expected = List.of("a", "b");
        assertEquals(expected, result);
    }

    @Test
    void findAllWithPrefixFiltersTags() {
        var recordA = JdbcPaimonYugabyteTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "release-1");
        var recordB = JdbcPaimonYugabyteTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "release-2");
        var recordC = JdbcPaimonYugabyteTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "snapshot-1");
        var repository = new JdbcTagRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> {
            repository.create(tx, recordA, false);
            repository.create(tx, recordB, false);
            repository.create(tx, recordC, false);
        });

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE, Optional.of("release")));

        var expected = List.of("release-1", "release-2");
        assertEquals(expected, result);
    }
}
