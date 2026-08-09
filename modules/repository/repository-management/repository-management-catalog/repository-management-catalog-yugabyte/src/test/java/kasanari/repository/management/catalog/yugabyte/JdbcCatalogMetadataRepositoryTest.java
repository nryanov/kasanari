package kasanari.repository.management.catalog.yugabyte;

import com.fasterxml.jackson.databind.ObjectMapper;
import kasanari.core.model.CatalogType;
import kasanari.fixtures.yugabyte.YugabyteFixtureContainer;
import kasanari.fixtures.yugabyte.YugabyteHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.management.catalog.model.CatalogMetadata;
import kasanari.repository.management.catalog.model.CatalogMode;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JdbcCatalogMetadataRepositoryTest {

    private static final YugabyteFixtureContainer YUGABYTE = new YugabyteFixtureContainer();
    private static YugabyteHelper yugabyteHelper;
    private static TransactionManager<Handle> txManager;
    private static JdbcCatalogMetadataRepository repository;

    @BeforeAll
    static void setup() {
        YUGABYTE.start();
        yugabyteHelper = new YugabyteHelper(YUGABYTE);
        repository = new JdbcCatalogMetadataRepository(new ObjectMapper());
    }

    @AfterAll
    static void cleanup() {
        JdbcManagementCatalogYugabyteTestHelper.close();
        YUGABYTE.stop();
    }

    @BeforeEach
    void beforeEach() {
        txManager = JdbcManagementCatalogYugabyteTestHelper.transactionManager(YUGABYTE);
        JdbcManagementCatalogYugabyteTestHelper.initializeSchema(txManager);
    }

    @AfterEach
    void afterEach() {
        JdbcManagementCatalogYugabyteTestHelper.truncateAll(yugabyteHelper);
    }

    @Test
    void createThenGetByNameReturnsRow() {
        var spec = JdbcManagementCatalogYugabyteTestHelper.catalogSpec();
        var inserted = new CatalogMetadata("warehouse", CatalogType.ICEBERG, CatalogMode.INTERNAL, spec, 1L);
        txManager.inTransaction(tx -> repository.create(tx, inserted));

        var result = txManager.inTransactionR(tx ->
                repository.getByName(tx, CatalogType.ICEBERG, "warehouse"));

        var expected = new CatalogMetadata("warehouse", CatalogType.ICEBERG, CatalogMode.INTERNAL, spec, 1L);
        assertEquals(expected, result.orElseThrow());
    }

    @Test
    void createDuplicateTypeAndNameReturnsFalse() {
        var metadata = new CatalogMetadata("dup", CatalogType.ICEBERG, CatalogMode.INTERNAL,
                JdbcManagementCatalogYugabyteTestHelper.catalogSpec(), 1L);
        txManager.inTransaction(tx -> repository.create(tx, metadata));

        var result = txManager.inTransactionR(tx -> repository.create(tx, metadata));

        var expected = false;
        assertEquals(expected, result);
    }

    @Test
    void sameNameDifferentTypesCoexist() {
        var spec = JdbcManagementCatalogYugabyteTestHelper.catalogSpec();
        txManager.inTransaction(tx -> {
            repository.create(tx, new CatalogMetadata("shared", CatalogType.ICEBERG, CatalogMode.INTERNAL, spec, 1L));
            repository.create(tx, new CatalogMetadata("shared", CatalogType.PAIMON, CatalogMode.INTERNAL, spec, 1L));
        });

        var icebergResult = txManager.inTransactionR(tx ->
                repository.getByName(tx, CatalogType.ICEBERG, "shared").isPresent());
        var paimonResult = txManager.inTransactionR(tx ->
                repository.getByName(tx, CatalogType.PAIMON, "shared").isPresent());

        var expected = true;
        assertEquals(expected, icebergResult);
        assertEquals(expected, paimonResult);
    }

    @Test
    void getByNameMissingReturnsEmpty() {
        var result = txManager.inTransactionR(tx -> repository.getByName(tx, CatalogType.LANCE, "nope"));

        var expected = Optional.empty();
        assertEquals(expected, result);
    }

    @Test
    void updateBumpsVersionAndPersistsSpec() {
        var insertedSpec = JdbcManagementCatalogYugabyteTestHelper.catalogSpec();
        var metadata = new CatalogMetadata("c", CatalogType.ICEBERG, CatalogMode.INTERNAL, insertedSpec, 1L);
        txManager.inTransaction(tx -> repository.create(tx, metadata));

        var newSpec = JdbcManagementCatalogYugabyteTestHelper.catalogSpec();

        var result = txManager.inTransactionR(tx ->
                repository.update(tx, CatalogType.ICEBERG, "c", newSpec, 1L));

        var expected = new CatalogMetadata("c", CatalogType.ICEBERG, CatalogMode.INTERNAL, newSpec, 2L);
        assertEquals(expected, result.orElseThrow());

        var loaded = txManager.inTransactionR(tx -> repository.getByName(tx, CatalogType.ICEBERG, "c"));
        assertEquals(expected, loaded.orElseThrow());
    }

    @Test
    void updateWithStaleExpectedVersionThrows() {
        var metadata = new CatalogMetadata("v", CatalogType.ICEBERG, CatalogMode.INTERNAL,
                JdbcManagementCatalogYugabyteTestHelper.catalogSpec(), 1L);
        txManager.inTransaction(tx -> repository.create(tx, metadata));

        assertThrows(IllegalStateException.class, () -> txManager.inTransactionR(tx ->
                repository.update(tx, CatalogType.ICEBERG, "v",
                        JdbcManagementCatalogYugabyteTestHelper.catalogSpec(), 99L)));
    }

    @Test
    void updateMissingCatalogReturnsEmpty() {
        var result = txManager.inTransactionR(tx ->
                repository.update(tx, CatalogType.ICEBERG, "missing",
                        JdbcManagementCatalogYugabyteTestHelper.catalogSpec(), 1L));

        var expected = Optional.empty();
        assertEquals(expected, result);
    }

    @Test
    void updateWithNullExpectedVersionSucceeds() {
        var spec = JdbcManagementCatalogYugabyteTestHelper.catalogSpec();
        var metadata = new CatalogMetadata("c", CatalogType.ICEBERG, CatalogMode.INTERNAL, spec, 1L);
        txManager.inTransaction(tx -> repository.create(tx, metadata));

        var newSpec = JdbcManagementCatalogYugabyteTestHelper.catalogSpec();

        var result = txManager.inTransactionR(tx ->
                repository.update(tx, CatalogType.ICEBERG, "c", newSpec, null));

        var expected = new CatalogMetadata("c", CatalogType.ICEBERG, CatalogMode.INTERNAL, newSpec, 2L);
        assertEquals(expected, result.orElseThrow());
    }

    @Test
    void deleteRemovesRow() {
        var metadata = new CatalogMetadata("d", CatalogType.ICEBERG, CatalogMode.INTERNAL,
                JdbcManagementCatalogYugabyteTestHelper.catalogSpec(), 1L);
        txManager.inTransaction(tx -> repository.create(tx, metadata));

        var result = txManager.inTransactionR(tx -> repository.delete(tx, CatalogType.ICEBERG, "d"));

        var expected = true;
        assertEquals(expected, result);

        var afterDelete = txManager.inTransactionR(tx -> repository.getByName(tx, CatalogType.ICEBERG, "d"));
        assertEquals(Optional.empty(), afterDelete);
    }

    @Test
    void deleteMissingReturnsFalse() {
        var result = txManager.inTransactionR(tx -> repository.delete(tx, CatalogType.ICEBERG, "missing"));

        var expected = false;
        assertEquals(expected, result);
    }

    @Test
    void listByTypeReturnsOnlyMatchingCatalogs() {
        var spec = JdbcManagementCatalogYugabyteTestHelper.catalogSpec();
        txManager.inTransaction(tx -> {
            repository.create(tx, new CatalogMetadata("alpha", CatalogType.ICEBERG, CatalogMode.INTERNAL, spec, 1L));
            repository.create(tx, new CatalogMetadata("beta", CatalogType.ICEBERG, CatalogMode.INTERNAL, spec, 1L));
            repository.create(tx, new CatalogMetadata("gamma", CatalogType.PAIMON, CatalogMode.INTERNAL, spec, 1L));
            repository.create(tx, new CatalogMetadata("delta", CatalogType.LANCE, CatalogMode.INTERNAL, spec, 1L));
        });

        var result = txManager.inTransactionR(tx -> repository.list(tx, CatalogType.ICEBERG));

        var expected = List.of(
                new CatalogMetadata("alpha", CatalogType.ICEBERG, CatalogMode.INTERNAL, spec, 1L),
                new CatalogMetadata("beta", CatalogType.ICEBERG, CatalogMode.INTERNAL, spec, 1L)
        );
        assertEquals(expected, result);
    }

    @Test
    void listByTypeEmptyWhenNone() {
        var result = txManager.inTransactionR(tx -> repository.list(tx, CatalogType.LANCE));

        var expected = List.of();
        assertEquals(expected, result);
    }
}
