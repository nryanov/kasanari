package kasanari.repository.iceberg.postgres;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
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

import static kasanari.repository.iceberg.postgres.JdbcIcebergPostgresTestHelper.DEFAULT_CATALOG;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcNamespaceRepositoryTest {

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
        JdbcIcebergPostgresTestHelper.close();
        POSTGRES.stop();
    }

    @BeforeEach
    void beforeEach() {
        txManager = JdbcIcebergPostgresTestHelper.transactionManager(POSTGRES);
        JdbcIcebergPostgresTestHelper.initializeSchema(txManager);
    }

    @AfterEach
    void afterEach() {
        JdbcIcebergPostgresTestHelper.truncateAll(postgresHelper);
    }

    @Test
    void createThenExistsReturnsTrue() {
        JdbcIcebergPostgresTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var namespace = Namespace.of("ns");
        var repository = new JdbcNamespaceRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, namespace, Map.of("k", "v")));

        var result = txManager.inTransactionR(tx -> repository.exists(tx, namespace));

        var expected = true;
        assertEquals(expected, result);
    }

    @Test
    void createThenLoadReturnsProperties() {
        JdbcIcebergPostgresTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var namespace = Namespace.of("ns");
        var repository = new JdbcNamespaceRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, namespace, Map.of("k", "v")));

        var result = txManager.inTransactionR(tx -> repository.load(tx, namespace));

        var expected = Map.of("k", "v");
        assertEquals(expected, result);
    }

    @Test
    void listRootReturnsTopLevelNamespaces() {
        JdbcIcebergPostgresTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
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
        JdbcIcebergPostgresTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
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
        JdbcIcebergPostgresTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var namespace = Namespace.of("ns");
        var repository = new JdbcNamespaceRepository(DEFAULT_CATALOG);
        txManager.inTransaction(tx -> repository.create(tx, namespace, Map.of()));

        var result = txManager.inTransactionR(tx -> repository.delete(tx, namespace));

        var expected = true;
        assertEquals(expected, result);
    }

    @Test
    void deleteMissingReturnsFalse() {
        JdbcIcebergPostgresTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var repository = new JdbcNamespaceRepository(DEFAULT_CATALOG);

        var result = txManager.inTransactionR(tx -> repository.delete(tx, Namespace.of("missing")));

        var expected = false;
        assertEquals(expected, result);
    }

    @Test
    void setPropertiesUpdatesValues() {
        JdbcIcebergPostgresTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
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
        JdbcIcebergPostgresTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
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
        JdbcIcebergPostgresTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var namespace = Namespace.of("ns");
        JdbcIcebergPostgresTestHelper.createNamespace(txManager, DEFAULT_CATALOG, namespace, Map.of());
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
        JdbcIcebergPostgresTestHelper.registerCatalog(txManager, DEFAULT_CATALOG);
        var namespace = Namespace.of("ns");
        JdbcIcebergPostgresTestHelper.createNamespace(txManager, DEFAULT_CATALOG, namespace, Map.of());
        var viewRepository = new JdbcViewRepository(DEFAULT_CATALOG);
        var view = TableIdentifier.of(namespace, "view");
        txManager.inTransaction(tx -> viewRepository.create(tx, view, "s3://warehouse/ns/view/metadata.json"));
        var repository = new JdbcNamespaceRepository(DEFAULT_CATALOG);

        var result = txManager.inTransactionR(tx -> repository.linkedViewsExist(tx, namespace));

        var expected = true;
        assertEquals(expected, result);
    }
}
