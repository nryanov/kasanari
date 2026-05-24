package kasanari.repository.iceberg.postgres;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.iceberg.model.IcebergTableRecord;
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

public class JdbcTableRepositoryTest {

    private static final PostgresFixtureContainer POSTGRES = new PostgresFixtureContainer();
    private static PostgresHelper postgresHelper;
    private static TransactionManager<Handle> txManager;

    private static final Namespace NAMESPACE = Namespace.of("ns");
    private static final String METADATA_LOCATION = "s3://warehouse/ns/table/metadata.json";
    private static final String UPDATED_METADATA_LOCATION = "s3://warehouse/ns/table/metadata-v2.json";

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
        var table = TableIdentifier.of(NAMESPACE, "table");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, table, METADATA_LOCATION));

        var result = txManager.inTransactionR(tx -> repository.exists(tx, table));

        var expected = true;
        assertEquals(expected, result);
    }

    @Test
    void createThenFindReturnsRecord() {
        var table = TableIdentifier.of(NAMESPACE, "table");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, table, METADATA_LOCATION));

        var result = txManager.inTransactionR(tx -> repository.find(tx, table));

        var expected = Optional.of(new IcebergTableRecord(
                DEFAULT_CATALOG, "ns", "table", METADATA_LOCATION, null));
        assertEquals(expected, result);
    }

    @Test
    void createThenFindUnsafeReturnsRecord() {
        var table = TableIdentifier.of(NAMESPACE, "table");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, table, METADATA_LOCATION));

        var result = txManager.inTransactionR(tx -> repository.findUnsafe(tx, table));

        var expected = new IcebergTableRecord(
                DEFAULT_CATALOG, "ns", "table", METADATA_LOCATION, null);
        assertEquals(expected, result);
    }

    @Test
    void findMissingReturnsEmpty() {
        var table = TableIdentifier.of(NAMESPACE, "missing");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG);

        var result = txManager.inTransactionR(tx -> repository.find(tx, table));

        var expected = Optional.empty();
        assertEquals(expected, result);
    }

    @Test
    void findUnsafeMissingThrows() {
        var table = TableIdentifier.of(NAMESPACE, "missing");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG);

        var result = assertThrows(NoSuchTableException.class, () ->
                txManager.inTransactionR(tx -> repository.findUnsafe(tx, table)));

        var expected = NoSuchTableException.class;
        assertEquals(expected, result.getClass());
    }

    @Test
    void updateWithMatchingPreviousLocationReturnsTrue() {
        var table = TableIdentifier.of(NAMESPACE, "table");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, table, METADATA_LOCATION));

        var updateResult = txManager.inTransactionR(tx -> repository.update(
                tx, table, METADATA_LOCATION, UPDATED_METADATA_LOCATION));
        var findResult = txManager.inTransactionR(tx -> repository.find(tx, table));

        var expectedUpdate = true;
        var expectedRecord = Optional.of(new IcebergTableRecord(
                DEFAULT_CATALOG, "ns", "table", UPDATED_METADATA_LOCATION, METADATA_LOCATION));
        assertEquals(expectedUpdate, updateResult);
        assertEquals(expectedRecord, findResult);
    }

    @Test
    void updateWithStalePreviousLocationReturnsFalse() {
        var table = TableIdentifier.of(NAMESPACE, "table");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, table, METADATA_LOCATION));

        var result = txManager.inTransactionR(tx -> repository.update(
                tx, table, "s3://warehouse/stale.json", UPDATED_METADATA_LOCATION));

        var expected = false;
        assertEquals(expected, result);
    }

    @Test
    void findByNamespaceReturnsCreatedTables() {
        var tableA = TableIdentifier.of(NAMESPACE, "a");
        var tableB = TableIdentifier.of(NAMESPACE, "b");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> {
            repository.create(tx, tableA, METADATA_LOCATION);
            repository.create(tx, tableB, METADATA_LOCATION);
        });

        var result = txManager.inTransactionR(tx -> repository.findByNamespace(tx, NAMESPACE));

        var expected = List.of(tableA, tableB);
        assertEquals(expected, result);
    }

    @Test
    void deleteExistingReturnsTrue() {
        var table = TableIdentifier.of(NAMESPACE, "table");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, table, METADATA_LOCATION));

        var deleteResult = txManager.inTransactionR(tx -> repository.delete(tx, table));
        var existsResult = txManager.inTransactionR(tx -> repository.exists(tx, table));

        var expectedDelete = true;
        var expectedExists = false;
        assertEquals(expectedDelete, deleteResult);
        assertEquals(expectedExists, existsResult);
    }

    @Test
    void renameExistingReturnsTrue() {
        var from = TableIdentifier.of(NAMESPACE, "from");
        var to = TableIdentifier.of(NAMESPACE, "to");
        var repository = new JdbcTableRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, from, METADATA_LOCATION));

        var renameResult = txManager.inTransactionR(tx -> repository.rename(tx, from, to));
        var existsResult = txManager.inTransactionR(tx -> repository.exists(tx, to));

        var expectedRename = true;
        var expectedExists = true;
        assertEquals(expectedRename, renameResult);
        assertEquals(expectedExists, existsResult);
    }
}
