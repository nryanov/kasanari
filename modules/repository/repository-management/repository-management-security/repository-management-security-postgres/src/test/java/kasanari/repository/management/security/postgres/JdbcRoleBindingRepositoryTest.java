package kasanari.repository.management.security.postgres;

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
        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "ICEBERG/"));

        var expected = List.of();
        assertEquals(expected, result);
    }

    @Test
    void upsertThenListBySubjectReturnsBinding() {
        var binding = new StoredRoleBinding("alice", "IcebergCatalogAdmin", "ICEBERG/warehouse/*");
        txManager.inTransaction(tx -> repository.upsert(tx, List.of(binding)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", null));

        var expected = List.of(binding);
        assertEquals(expected, result);
    }

    @Test
    void upsertThenListByResourcePrefixReturnsBinding() {
        var binding = new StoredRoleBinding("alice", "IcebergCatalogAdmin", "ICEBERG/warehouse/*");
        txManager.inTransaction(tx -> repository.upsert(tx, List.of(binding)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, null, "ICEBERG/warehouse/"));

        var expected = List.of(binding);
        assertEquals(expected, result);
    }

    @Test
    void upsertThenListWithNullFiltersReturnsAllBindings() {
        var aliceBinding = new StoredRoleBinding("alice", "IcebergCatalogAdmin", "ICEBERG/warehouse/*");
        var bobBinding = new StoredRoleBinding("bob", "PaimonCatalogViewer", "PAIMON/events/*");
        txManager.inTransaction(tx -> repository.upsert(tx, List.of(aliceBinding, bobBinding)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, null, null));

        var expected = List.of(aliceBinding, bobBinding);
        assertEquals(expected, result);
    }

    @Test
    void listFiltersBySubjectAndResourcePrefix() {
        var matching = new StoredRoleBinding("alice", "IcebergCatalogAdmin", "ICEBERG/warehouse/*");
        var otherSubject = new StoredRoleBinding("bob", "IcebergCatalogAdmin", "ICEBERG/warehouse/*");
        var otherResource = new StoredRoleBinding("alice", "PaimonCatalogAdmin", "PAIMON/events/*");
        txManager.inTransaction(tx -> repository.upsert(tx, List.of(matching, otherSubject, otherResource)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "ICEBERG/"));

        var expected = List.of(matching);
        assertEquals(expected, result);
    }

    @Test
    void upsertMultipleBindingsThenListReturnsSorted() {
        var charlie = new StoredRoleBinding("charlie", "LanceCatalogEditor", "LANCE/lake/*");
        var alice = new StoredRoleBinding("alice", "IcebergCatalogAdmin", "ICEBERG/warehouse/*");
        var bob = new StoredRoleBinding("bob", "PaimonCatalogViewer", "PAIMON/events/*");
        txManager.inTransaction(tx -> repository.upsert(tx, List.of(charlie, alice, bob)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, null, null));

        var expected = List.of(alice, bob, charlie);
        assertEquals(expected, result);
    }

    @Test
    void deleteRemovesBinding() {
        var binding = new StoredRoleBinding("alice", "IcebergCatalogAdmin", "ICEBERG/warehouse/*");
        txManager.inTransaction(tx -> repository.upsert(tx, List.of(binding)));
        txManager.inTransaction(tx -> repository.delete(tx, List.of(binding)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "ICEBERG/"));

        var expected = List.of();
        assertEquals(expected, result);
    }

    @Test
    void deleteOnlyRemovesMatchingBindings() {
        var toDelete = new StoredRoleBinding("alice", "IcebergCatalogAdmin", "ICEBERG/warehouse/*");
        var toKeep = new StoredRoleBinding("alice", "IcebergCatalogViewer", "ICEBERG/warehouse/analytics/*");
        txManager.inTransaction(tx -> repository.upsert(tx, List.of(toDelete, toKeep)));
        txManager.inTransaction(tx -> repository.delete(tx, List.of(toDelete)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "ICEBERG/"));

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
        var binding = new StoredRoleBinding("alice", "IcebergCatalogAdmin", "ICEBERG/warehouse/*");
        txManager.inTransaction(tx -> repository.upsert(tx, List.of(binding)));
        txManager.inTransaction(tx -> repository.delete(tx, List.of()));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "ICEBERG/"));

        var expected = List.of(binding);
        assertEquals(expected, result);
    }

    @Test
    void upsertDuplicateBindingKeepsSingleRow() {
        var binding = new StoredRoleBinding("alice", "IcebergCatalogAdmin", "ICEBERG/warehouse/*");
        txManager.inTransaction(tx -> {
            repository.upsert(tx, List.of(binding));
            repository.upsert(tx, List.of(binding));
        });

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "ICEBERG/"));

        var expected = List.of(binding);
        assertEquals(expected, result);
    }

    @Test
    void sameRoleDifferentResourcesAreDistinctRows() {
        var catalogScope = new StoredRoleBinding("alice", "IcebergCatalogViewer", "ICEBERG/warehouse/*");
        var namespaceScope = new StoredRoleBinding("alice", "IcebergCatalogEditor", "ICEBERG/warehouse/analytics/*");
        txManager.inTransaction(tx -> repository.upsert(tx, List.of(catalogScope, namespaceScope)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "ICEBERG/"));

        var expected = List.of(catalogScope, namespaceScope);
        assertEquals(expected, result);
    }
}
