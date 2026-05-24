package kasanari.repository.paimon.postgres;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
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

import static kasanari.repository.paimon.postgres.JdbcPaimonPostgresTestHelper.DEFAULT_CATALOG_KEY;
import static kasanari.repository.paimon.postgres.JdbcPaimonPostgresTestHelper.DEFAULT_DATABASE;
import static kasanari.repository.paimon.postgres.JdbcPaimonPostgresTestHelper.DEFAULT_TABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcTagRepositoryTest {

    private static final PostgresFixtureContainer POSTGRES = new PostgresFixtureContainer();
    private static PostgresHelper postgresHelper;
    private static TransactionManager<Handle> txManager;

    private static final Identifier TABLE = Identifier.create(DEFAULT_DATABASE, DEFAULT_TABLE);

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
        JdbcPaimonPostgresTestHelper.createDatabaseAndTable(txManager, DEFAULT_CATALOG_KEY);
    }

    @AfterEach
    void afterEach() {
        JdbcPaimonPostgresTestHelper.truncateAll(postgresHelper);
    }

    @Test
    void createThenFindReturnsRecord() {
        var record = JdbcPaimonPostgresTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "v1");
        var repository = new JdbcTagRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.create(tx, record, false));

        var result = txManager.inTransactionR(tx -> repository.find(tx, TABLE, "v1"));

        var expected = Optional.of(record);
        assertEquals(expected, result);
    }

    @Test
    void createIgnoreIfExistsDoesNotFail() {
        var record = JdbcPaimonPostgresTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "v1");
        var repository = new JdbcTagRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.create(tx, record, false));
        txManager.inTransaction(tx -> repository.create(tx, record, true));

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE, Optional.empty()));

        var expected = List.of("v1");
        assertEquals(expected, result);
    }

    @Test
    void existsReturnsTrueAfterCreate() {
        var record = JdbcPaimonPostgresTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "v1");
        var repository = new JdbcTagRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.create(tx, record, false));

        var result = txManager.inTransactionR(tx -> repository.exists(tx, TABLE, "v1"));

        var expected = true;
        assertEquals(expected, result);
    }

    @Test
    void deleteExistingReturnsTrue() {
        var record = JdbcPaimonPostgresTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "v1");
        var repository = new JdbcTagRepository(DEFAULT_CATALOG_KEY);
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
        var repository = new JdbcTagRepository(DEFAULT_CATALOG_KEY);

        var result = txManager.inTransactionR(tx -> repository.delete(tx, TABLE, "missing"));

        var expected = false;
        assertEquals(expected, result);
    }

    @Test
    void findAllReturnsTagNames() {
        var recordA = JdbcPaimonPostgresTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "a");
        var recordB = JdbcPaimonPostgresTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "b");
        var repository = new JdbcTagRepository(DEFAULT_CATALOG_KEY);
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
        var recordA = JdbcPaimonPostgresTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "release-1");
        var recordB = JdbcPaimonPostgresTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "release-2");
        var recordC = JdbcPaimonPostgresTestHelper.tagRecord(DEFAULT_DATABASE, DEFAULT_TABLE, "snapshot-1");
        var repository = new JdbcTagRepository(DEFAULT_CATALOG_KEY);
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
