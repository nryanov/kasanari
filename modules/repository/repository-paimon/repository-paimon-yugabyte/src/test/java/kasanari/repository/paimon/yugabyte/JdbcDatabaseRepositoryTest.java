package kasanari.repository.paimon.yugabyte;

import kasanari.fixtures.yugabyte.YugabyteFixtureContainer;
import kasanari.fixtures.yugabyte.YugabyteHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.paimon.model.DatabaseRecord;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static kasanari.repository.paimon.yugabyte.JdbcPaimonYugabyteTestHelper.DEFAULT_CATALOG_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcDatabaseRepositoryTest {

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
    }

    @AfterEach
    void afterEach() {
        JdbcPaimonYugabyteTestHelper.truncateAll(yugabyteHelper);
    }

    @Test
    void createThenFindByNameReturnsRecord() {
        var record = new DatabaseRecord("my_db", Map.of("k", "v"), Optional.of("comment"));
        var repository = new JdbcDatabaseRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.create(tx, record));

        var result = txManager.inTransactionR(tx -> repository.findByName(tx, "my_db"));

        var expected = Optional.of(record);
        assertEquals(expected, result);
    }

    @Test
    void findByNameMissingReturnsEmpty() {
        var repository = new JdbcDatabaseRepository(DEFAULT_CATALOG_NAME);

        var result = txManager.inTransactionR(tx -> repository.findByName(tx, "missing"));

        var expected = Optional.empty();
        assertEquals(expected, result);
    }

    @Test
    void deleteExistingReturnsTrue() {
        var record = JdbcPaimonYugabyteTestHelper.databaseRecord("delete_me");
        var repository = new JdbcDatabaseRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.create(tx, record));

        var result = txManager.inTransactionR(tx -> repository.delete(tx, "delete_me"));

        var expected = true;
        assertEquals(expected, result);
    }

    @Test
    void deleteMissingReturnsFalse() {
        var repository = new JdbcDatabaseRepository(DEFAULT_CATALOG_NAME);

        var result = txManager.inTransactionR(tx -> repository.delete(tx, "missing"));

        var expected = false;
        assertEquals(expected, result);
    }

    @Test
    void alterMergesOptions() {
        var record = new DatabaseRecord("alter_db", Map.of("keep", "yes", "remove", "no"), Optional.empty());
        var repository = new JdbcDatabaseRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.create(tx, record));
        txManager.inTransaction(tx -> repository.alter(tx, "alter_db", Map.of("keep", "updated", "new", "value"), Set.of("remove")));

        var result = txManager.inTransactionR(tx -> repository.findByName(tx, "alter_db"));

        var expected = Optional.of(new DatabaseRecord("alter_db", Map.of("keep", "updated", "new", "value"), Optional.empty()));
        assertEquals(expected, result);
    }

    @Test
    void findAllReturnsCreatedDatabases() {
        var recordA = JdbcPaimonYugabyteTestHelper.databaseRecord("a");
        var recordB = JdbcPaimonYugabyteTestHelper.databaseRecord("b");
        var repository = new JdbcDatabaseRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> {
            repository.create(tx, recordA);
            repository.create(tx, recordB);
        });

        var result = txManager.inTransactionR(repository::findAll);

        var expected = List.of(recordA, recordB);
        assertEquals(expected, result);
    }
}
