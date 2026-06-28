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
        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "ICEBERG/warehouse"));

        var expected = List.of();
        assertEquals(expected, result);
    }

    @Test
    void addThenListBySubjectAndResourceReturnsBinding() {
        var binding = new StoredRoleBinding("alice", "ICEBERG/warehouse", "IcebergCatalogAdmin");
        txManager.inTransaction(tx -> repository.add(tx, List.of(binding)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "ICEBERG/warehouse"));

        var expected = List.of(binding);
        assertEquals(expected, result);
    }

    @Test
    void addThenListByResourceReturnsAllSubjectsAtResource() {
        var binding = new StoredRoleBinding("alice", "ICEBERG/warehouse", "IcebergCatalogAdmin");
        txManager.inTransaction(tx -> repository.add(tx, List.of(binding)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, null, "ICEBERG/warehouse"));

        var expected = List.of(binding);
        assertEquals(expected, result);
    }

    @Test
    void addThenListAllReturnsAllBindings() {
        var aliceBinding = new StoredRoleBinding("alice", "ICEBERG/warehouse", "IcebergCatalogAdmin");
        var bobBinding = new StoredRoleBinding("bob", "PAIMON/events", "PaimonCatalogViewer");
        txManager.inTransaction(tx -> repository.add(tx, List.of(aliceBinding, bobBinding)));

        var result = txManager.inTransactionR(tx -> repository.listAll(tx));

        var expected = List.of(aliceBinding, bobBinding);
        assertEquals(expected, result);
    }

    @Test
    void listFiltersBySubjectAndExactResource() {
        var matching = new StoredRoleBinding("alice", "ICEBERG/warehouse", "IcebergCatalogAdmin");
        var otherSubject = new StoredRoleBinding("bob", "ICEBERG/warehouse", "IcebergCatalogAdmin");
        var otherResource = new StoredRoleBinding("alice", "PAIMON/events", "PaimonCatalogAdmin");
        txManager.inTransaction(tx -> repository.add(tx, List.of(matching, otherSubject, otherResource)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "ICEBERG/warehouse"));

        var expected = List.of(matching);
        assertEquals(expected, result);
    }

    @Test
    void listDoesNotMatchResourcePrefix() {
        var binding = new StoredRoleBinding("alice", "ICEBERG/warehouse", "IcebergCatalogAdmin");
        txManager.inTransaction(tx -> repository.add(tx, List.of(binding)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "ICEBERG/warehouse/analytics"));

        var expected = List.of();
        assertEquals(expected, result);
    }

    @Test
    void addMultipleBindingsThenListAllReturnsSorted() {
        var charlie = new StoredRoleBinding("charlie", "LANCE/lake", "LanceCatalogEditor");
        var alice = new StoredRoleBinding("alice", "ICEBERG/warehouse", "IcebergCatalogAdmin");
        var bob = new StoredRoleBinding("bob", "PAIMON/events", "PaimonCatalogViewer");
        txManager.inTransaction(tx -> repository.add(tx, List.of(charlie, alice, bob)));

        var result = txManager.inTransactionR(tx -> repository.listAll(tx));

        var expected = List.of(alice, bob, charlie);
        assertEquals(expected, result);
    }

    @Test
    void deleteRemovesBinding() {
        var binding = new StoredRoleBinding("alice", "ICEBERG/warehouse", "IcebergCatalogAdmin");
        txManager.inTransaction(tx -> repository.add(tx, List.of(binding)));
        var beforeDelete = txManager.inTransactionR(tx -> repository.list(tx, "alice", "ICEBERG/warehouse"));
        assertEquals(1, beforeDelete.size());

        txManager.inTransaction(tx -> repository.delete(tx, List.of(binding)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "ICEBERG/warehouse"));

        var expected = List.of();
        assertEquals(expected, result);
    }

    @Test
    void deleteOnlyRemovesMatchingBindings() {
        var toDelete = new StoredRoleBinding("alice", "ICEBERG/warehouse", "IcebergCatalogAdmin");
        var toKeep = new StoredRoleBinding("alice", "ICEBERG/warehouse/analytics", "IcebergCatalogViewer");
        txManager.inTransaction(tx -> repository.add(tx, List.of(toDelete, toKeep)));

        txManager.inTransaction(tx -> repository.delete(tx, List.of(toDelete)));

        var result = txManager.inTransactionR(tx -> repository.listAll(tx));

        var expected = List.of(toKeep);
        assertEquals(expected, result);
    }

    @Test
    void addEmptyListIsNoOp() {
        txManager.inTransaction(tx -> repository.add(tx, List.of()));

        var result = txManager.inTransactionR(tx -> repository.listAll(tx));

        var expected = List.of();
        assertEquals(expected, result);
    }

    @Test
    void deleteEmptyListIsNoOp() {
        var binding = new StoredRoleBinding("alice", "ICEBERG/warehouse", "IcebergCatalogAdmin");
        txManager.inTransaction(tx -> repository.add(tx, List.of(binding)));
        txManager.inTransaction(tx -> repository.delete(tx, List.of()));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "ICEBERG/warehouse"));

        var expected = List.of(binding);
        assertEquals(expected, result);
    }

    @Test
    void addDuplicateBindingKeepsSingleRow() {
        var binding = new StoredRoleBinding("alice", "ICEBERG/warehouse", "IcebergCatalogAdmin");
        txManager.inTransaction(tx -> {
            repository.add(tx, List.of(binding));
            repository.add(tx, List.of(binding));
        });

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "ICEBERG/warehouse"));

        var expected = List.of(binding);
        assertEquals(expected, result);
    }

    @Test
    void sameRoleDifferentResourcesAreDistinctRows() {
        var catalogScope = new StoredRoleBinding("alice", "ICEBERG/warehouse", "IcebergCatalogEditor");
        var namespaceScope = new StoredRoleBinding("alice", "ICEBERG/warehouse/analytics", "IcebergCatalogEditor");
        txManager.inTransaction(tx -> repository.add(tx, List.of(catalogScope, namespaceScope)));

        var catalogResult = txManager.inTransactionR(tx -> repository.list(tx, "alice", "ICEBERG/warehouse"));
        var namespaceResult = txManager.inTransactionR(tx -> repository.list(tx, "alice", "ICEBERG/warehouse/analytics"));

        assertEquals(List.of(catalogScope), catalogResult);
        assertEquals(List.of(namespaceScope), namespaceResult);
    }
}
