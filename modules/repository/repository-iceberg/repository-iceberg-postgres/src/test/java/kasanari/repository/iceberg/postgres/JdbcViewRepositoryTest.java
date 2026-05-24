package kasanari.repository.iceberg.postgres;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.iceberg.model.IcebergViewRecord;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static kasanari.repository.iceberg.postgres.JdbcIcebergPostgresTestHelper.DEFAULT_CATALOG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcViewRepositoryTest {

    private static final PostgresFixtureContainer POSTGRES = new PostgresFixtureContainer();
    private static PostgresHelper postgresHelper;
    private static TransactionManager<Handle> txManager;

    private static final Namespace NAMESPACE = Namespace.of("ns");
    private static final String METADATA_LOCATION = "s3://warehouse/ns/view/metadata.json";
    private static final String UPDATED_METADATA_LOCATION = "s3://warehouse/ns/view/metadata-v2.json";

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
        JdbcIcebergPostgresTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        JdbcIcebergPostgresTestHelper.createNamespace(txManager, DEFAULT_CATALOG, NAMESPACE, Map.of());
    }

    @AfterEach
    void afterEach() {
        JdbcIcebergPostgresTestHelper.truncateAll(postgresHelper);
    }

    @Test
    void createThenExistsReturnsTrue() {
        var view = TableIdentifier.of(NAMESPACE, "view");
        var repository = new JdbcViewRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, view, METADATA_LOCATION));

        var result = txManager.inTransactionR(tx -> repository.exists(tx, view));

        var expected = true;
        assertEquals(expected, result);
    }

    @Test
    void createThenFindReturnsRecord() {
        var view = TableIdentifier.of(NAMESPACE, "view");
        var repository = new JdbcViewRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, view, METADATA_LOCATION));

        var result = txManager.inTransactionR(tx -> repository.find(tx, view));

        var expected = Optional.of(new IcebergViewRecord(
                DEFAULT_CATALOG, "ns", "view", METADATA_LOCATION, null));
        assertEquals(expected, result);
    }

    @Test
    void createThenFindUnsafeReturnsRecord() {
        var view = TableIdentifier.of(NAMESPACE, "view");
        var repository = new JdbcViewRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, view, METADATA_LOCATION));

        var result = txManager.inTransactionR(tx -> repository.findUnsafe(tx, view));

        var expected = new IcebergViewRecord(
                DEFAULT_CATALOG, "ns", "view", METADATA_LOCATION, null);
        assertEquals(expected, result);
    }

    @Test
    void findMissingReturnsEmpty() {
        var view = TableIdentifier.of(NAMESPACE, "missing");
        var repository = new JdbcViewRepository(DEFAULT_CATALOG);

        var result = txManager.inTransactionR(tx -> repository.find(tx, view));

        var expected = Optional.empty();
        assertEquals(expected, result);
    }

    @Test
    void findUnsafeMissingThrows() {
        var view = TableIdentifier.of(NAMESPACE, "missing");
        var repository = new JdbcViewRepository(DEFAULT_CATALOG);

        var result = assertThrows(NoSuchTableException.class, () ->
                txManager.inTransactionR(tx -> repository.findUnsafe(tx, view)));

        var expected = NoSuchTableException.class;
        assertEquals(expected, result.getClass());
    }

    @Test
    void updateWithMatchingPreviousLocationReturnsTrue() {
        var view = TableIdentifier.of(NAMESPACE, "view");
        var repository = new JdbcViewRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, view, METADATA_LOCATION));

        var updateResult = txManager.inTransactionR(tx -> repository.update(
                tx, view, METADATA_LOCATION, UPDATED_METADATA_LOCATION));
        var findResult = txManager.inTransactionR(tx -> repository.find(tx, view));

        var expectedUpdate = true;
        var expectedRecord = Optional.of(new IcebergViewRecord(
                DEFAULT_CATALOG, "ns", "view", UPDATED_METADATA_LOCATION, METADATA_LOCATION));
        assertEquals(expectedUpdate, updateResult);
        assertEquals(expectedRecord, findResult);
    }

    @Test
    void updateWithStalePreviousLocationReturnsFalse() {
        var view = TableIdentifier.of(NAMESPACE, "view");
        var repository = new JdbcViewRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, view, METADATA_LOCATION));

        var result = txManager.inTransactionR(tx -> repository.update(
                tx, view, "s3://warehouse/stale.json", UPDATED_METADATA_LOCATION));

        var expected = false;
        assertEquals(expected, result);
    }

    @Test
    void findByNamespaceReturnsCreatedViews() {
        var viewA = TableIdentifier.of(NAMESPACE, "a");
        var viewB = TableIdentifier.of(NAMESPACE, "b");
        var repository = new JdbcViewRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> {
            repository.create(tx, viewA, METADATA_LOCATION);
            repository.create(tx, viewB, METADATA_LOCATION);
        });

        var result = txManager.inTransactionR(tx -> repository.findByNamespace(tx, NAMESPACE));

        var expected = List.of(viewA, viewB);
        assertEquals(expected, result);
    }

    @Test
    void deleteExistingReturnsTrue() {
        var view = TableIdentifier.of(NAMESPACE, "view");
        var repository = new JdbcViewRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, view, METADATA_LOCATION));

        var deleteResult = txManager.inTransactionR(tx -> repository.delete(tx, view));
        var existsResult = txManager.inTransactionR(tx -> repository.exists(tx, view));

        var expectedDelete = true;
        var expectedExists = false;
        assertEquals(expectedDelete, deleteResult);
        assertEquals(expectedExists, existsResult);
    }

    @Test
    void renameExistingReturnsTrue() {
        var from = TableIdentifier.of(NAMESPACE, "from");
        var to = TableIdentifier.of(NAMESPACE, "to");
        var repository = new JdbcViewRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, from, METADATA_LOCATION));

        var renameResult = txManager.inTransactionR(tx -> repository.rename(tx, from, to));
        var existsResult = txManager.inTransactionR(tx -> repository.exists(tx, to));

        var expectedRename = true;
        var expectedExists = true;
        assertEquals(expectedRename, renameResult);
        assertEquals(expectedExists, existsResult);
    }
}
