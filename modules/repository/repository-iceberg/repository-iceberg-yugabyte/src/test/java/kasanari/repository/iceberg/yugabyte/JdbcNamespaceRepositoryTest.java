package kasanari.repository.iceberg.yugabyte;

import kasanari.fixtures.yugabyte.YugabyteFixtureContainer;
import kasanari.fixtures.yugabyte.YugabyteHelper;
import kasanari.repository.core.TransactionManager;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static kasanari.repository.iceberg.yugabyte.JdbcIcebergYugabyteTestHelper.DEFAULT_CATALOG;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcNamespaceRepositoryTest {

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
        JdbcIcebergYugabyteTestHelper.close();
        YUGABYTE.stop();
    }

    @BeforeEach
    void beforeEach() {
        txManager = JdbcIcebergYugabyteTestHelper.transactionManager(YUGABYTE);
        JdbcIcebergYugabyteTestHelper.initializeSchema(txManager);
    }

    @AfterEach
    void afterEach() {
        JdbcIcebergYugabyteTestHelper.truncateAll(yugabyteHelper);
    }

    @Test
    void createThenExistsReturnsTrue() {
        JdbcIcebergYugabyteTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var namespace = Namespace.of("ns");
        var repository = new JdbcNamespaceRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, namespace, Map.of("k", "v")));

        var result = txManager.inTransactionR(tx -> repository.exists(tx, namespace));

        var expected = true;
        assertEquals(expected, result);
    }

    @Test
    void createThenLoadReturnsProperties() {
        JdbcIcebergYugabyteTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var namespace = Namespace.of("ns");
        var repository = new JdbcNamespaceRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, namespace, Map.of("k", "v")));

        var result = txManager.inTransactionR(tx -> repository.load(tx, namespace));

        var expected = Map.of("k", "v");
        assertEquals(expected, result);
    }

    @Test
    void listRootReturnsTopLevelNamespaces() {
        JdbcIcebergYugabyteTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var repository = new JdbcNamespaceRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> {
            repository.create(tx, Namespace.of("a"), Map.of());
            repository.create(tx, Namespace.of("b"), Map.of());
        });

        var result = txManager.inTransactionR(tx -> repository.list(tx, Namespace.empty()));

        var expected = List.of(Namespace.of("a"), Namespace.of("b"));
        assertEquals(expected, result);
    }

    @Test
    void listChildrenReturnsDirectChildren() {
        JdbcIcebergYugabyteTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var repository = new JdbcNamespaceRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> {
            repository.create(tx, Namespace.of("parent"), Map.of());
            repository.create(tx, Namespace.of("parent", "child"), Map.of());
        });

        var result = txManager.inTransactionR(tx -> repository.list(tx, Namespace.of("parent")));

        var expected = List.of(Namespace.of("parent", "child"));
        assertEquals(expected, result);
    }

    @Test
    void deleteExistingReturnsTrue() {
        JdbcIcebergYugabyteTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var namespace = Namespace.of("ns");
        var repository = new JdbcNamespaceRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, namespace, Map.of()));

        var result = txManager.inTransactionR(tx -> repository.delete(tx, namespace));

        var expected = true;
        assertEquals(expected, result);
    }

    @Test
    void deleteMissingReturnsFalse() {
        JdbcIcebergYugabyteTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var repository = new JdbcNamespaceRepository(DEFAULT_CATALOG);

        var result = txManager.inTransactionR(tx -> repository.delete(tx, Namespace.of("missing")));

        var expected = false;
        assertEquals(expected, result);
    }

    @Test
    void setPropertiesUpdatesValues() {
        JdbcIcebergYugabyteTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var namespace = Namespace.of("ns");
        var repository = new JdbcNamespaceRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, namespace, Map.of("k", "v")));
        txManager.inTransaction(tx -> repository.setProperties(tx, namespace, Map.of("k", "updated", "new", "value")));

        var result = txManager.inTransactionR(tx -> repository.load(tx, namespace));

        var expected = Map.of("k", "updated", "new", "value");
        assertEquals(expected, result);
    }

    @Test
    void removePropertiesRemovesKeys() {
        JdbcIcebergYugabyteTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var namespace = Namespace.of("ns");
        var repository = new JdbcNamespaceRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, namespace, Map.of("keep", "yes", "remove", "no")));
        txManager.inTransaction(tx -> repository.removeProperties(tx, namespace, Set.of("remove")));

        var result = txManager.inTransactionR(tx -> repository.load(tx, namespace));

        var expected = Map.of("keep", "yes");
        assertEquals(expected, result);
    }

    @Test
    void linkedTablesExistWhenTablePresent() {
        JdbcIcebergYugabyteTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var namespace = Namespace.of("ns");
        JdbcIcebergYugabyteTestHelper.createNamespace(txManager, DEFAULT_CATALOG, namespace, Map.of());
        var tableRepository = new JdbcTableRepository(DEFAULT_CATALOG);
        var table = TableIdentifier.of(namespace, "table");
        txManager.inTransaction(tx -> tableRepository.create(tx, table, "s3://warehouse/ns/table/metadata.json"));
        var repository = new JdbcNamespaceRepository(DEFAULT_CATALOG);

        var result = txManager.inTransactionR(tx -> repository.linkedTablesExist(tx, namespace));

        var expected = true;
        assertEquals(expected, result);
    }

    @Test
    void linkedViewsExistWhenViewPresent() {
        JdbcIcebergYugabyteTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var namespace = Namespace.of("ns");
        JdbcIcebergYugabyteTestHelper.createNamespace(txManager, DEFAULT_CATALOG, namespace, Map.of());
        var viewRepository = new JdbcViewRepository(DEFAULT_CATALOG);
        var view = TableIdentifier.of(namespace, "view");
        txManager.inTransaction(tx -> viewRepository.create(tx, view, "s3://warehouse/ns/view/metadata.json"));
        var repository = new JdbcNamespaceRepository(DEFAULT_CATALOG);

        var result = txManager.inTransactionR(tx -> repository.linkedViewsExist(tx, namespace));

        var expected = true;
        assertEquals(expected, result);
    }
}
