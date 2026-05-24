package kasanari.repository.management.security.postgres;

import kasanari.core.model.CatalogType;
import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.management.security.model.StoredRoleBinding;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcRoleBindingRepositoryTest {

    private static final PostgresFixtureContainer POSTGRES = new PostgresFixtureContainer();
    private static JdbcManagementSecurityPostgresTestHelper helper;
    private static JdbcRoleBindingRepository repository;
    private static TransactionManager<Handle> txManager;

    @BeforeAll
    static void setup() {
        POSTGRES.start();
        helper = new JdbcManagementSecurityPostgresTestHelper(POSTGRES);
        repository = new JdbcRoleBindingRepository();
        helper.initializeSchema();

        txManager = helper.transactionManager();
    }

    @AfterAll
    static void cleanup() {
        helper.close();
        POSTGRES.stop();
    }

    @BeforeEach
    void beforeEach() {
        helper.truncateAll();
    }

    @Test
    void listWhenEmptyReturnsEmptyList() {
        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", CatalogType.ICEBERG));

        var expected = List.of();
        assertEquals(expected, result);
    }

    @Test
    void upsertThenListBySubjectReturnsBinding() {
        var binding = new StoredRoleBinding("alice", CatalogType.ICEBERG, "admin");
        txManager.inTransaction(tx -> repository.upsert(tx, List.of(binding)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", null));

        var expected = List.of(binding);
        assertEquals(expected, result);
    }

    @Test
    void upsertThenListByCatalogTypeReturnsBinding() {
        var binding = new StoredRoleBinding("alice", CatalogType.ICEBERG, "admin");
        txManager.inTransaction(tx -> repository.upsert(tx, List.of(binding)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, null, CatalogType.ICEBERG));

        var expected = List.of(binding);
        assertEquals(expected, result);
    }

    @Test
    void upsertThenListWithNullFiltersReturnsAllBindings() {
        var aliceBinding = new StoredRoleBinding("alice", CatalogType.ICEBERG, "admin");
        var bobBinding = new StoredRoleBinding("bob", CatalogType.PAIMON, "reader");
        txManager.inTransaction(tx -> repository.upsert(tx, List.of(aliceBinding, bobBinding)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, null, null));

        var expected = List.of(aliceBinding, bobBinding);
        assertEquals(expected, result);
    }

    @Test
    void listFiltersBySubjectAndCatalogType() {
        var matching = new StoredRoleBinding("alice", CatalogType.ICEBERG, "admin");
        var otherSubject = new StoredRoleBinding("bob", CatalogType.ICEBERG, "admin");
        var otherCatalog = new StoredRoleBinding("alice", CatalogType.PAIMON, "admin");
        txManager.inTransaction(tx -> repository.upsert(tx, List.of(matching, otherSubject, otherCatalog)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", CatalogType.ICEBERG));

        var expected = List.of(matching);
        assertEquals(expected, result);
    }

    @Test
    void upsertMultipleBindingsThenListReturnsSorted() {
        var charlie = new StoredRoleBinding("charlie", CatalogType.LANCE, "writer");
        var alice = new StoredRoleBinding("alice", CatalogType.ICEBERG, "admin");
        var bob = new StoredRoleBinding("bob", CatalogType.PAIMON, "reader");
        txManager.inTransaction(tx -> repository.upsert(tx, List.of(charlie, alice, bob)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, null, null));

        var expected = List.of(alice, bob, charlie);
        assertEquals(expected, result);
    }

    @Test
    void deleteRemovesBinding() {
        var binding = new StoredRoleBinding("alice", CatalogType.ICEBERG, "admin");
        txManager.inTransaction(tx -> repository.upsert(tx, List.of(binding)));
        txManager.inTransaction(tx -> repository.delete(tx, List.of(binding)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", CatalogType.ICEBERG));

        var expected = List.of();
        assertEquals(expected, result);
    }

    @Test
    void deleteOnlyRemovesMatchingBindings() {
        var toDelete = new StoredRoleBinding("alice", CatalogType.ICEBERG, "admin");
        var toKeep = new StoredRoleBinding("alice", CatalogType.ICEBERG, "reader");
        txManager.inTransaction(tx -> repository.upsert(tx, List.of(toDelete, toKeep)));
        txManager.inTransaction(tx -> repository.delete(tx, List.of(toDelete)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", CatalogType.ICEBERG));

        var expected = List.of(toKeep);
        assertEquals(expected, result);
    }

    @Test
    void upsertEmptyListIsNoOp() {
        txManager.inTransaction(tx -> repository.upsert(tx, List.of()));

        var result = txManager.inTransactionR(tx -> repository.list(tx, null, null));

        var expected = List.of();
        assertEquals(expected, result);
    }

    @Test
    void deleteEmptyListIsNoOp() {
        var binding = new StoredRoleBinding("alice", CatalogType.ICEBERG, "admin");
        txManager.inTransaction(tx -> repository.upsert(tx, List.of(binding)));
        txManager.inTransaction(tx -> repository.delete(tx, List.of()));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", CatalogType.ICEBERG));

        var expected = List.of(binding);
        assertEquals(expected, result);
    }

    @Test
    void upsertDuplicateBindingKeepsSingleRow() {
        var binding = new StoredRoleBinding("alice", CatalogType.ICEBERG, "admin");
        txManager.inTransaction(tx -> {
            repository.upsert(tx, List.of(binding));
            repository.upsert(tx, List.of(binding));
        });

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", CatalogType.ICEBERG));

        var expected = List.of(binding);
        assertEquals(expected, result);
    }
}
