package kasanari.repository.management.security.yugabyte;

import kasanari.fixtures.yugabyte.YugabyteFixtureContainer;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.management.security.model.StoredRoleBinding;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JdbcRoleBindingRepositoryTest {

    private static final YugabyteFixtureContainer YUGABYTE = new YugabyteFixtureContainer();
    private static JdbcManagementSecurityYugabyteTestHelper helper;
    private static JdbcRoleBindingRepository repository;
    private static TransactionManager<Handle> txManager;

    @BeforeAll
    static void setup() {
        YUGABYTE.start();
        helper = new JdbcManagementSecurityYugabyteTestHelper(YUGABYTE);
        repository = new JdbcRoleBindingRepository();
        helper.initializeSchema();

        txManager = helper.transactionManager();
    }

    @AfterAll
    static void cleanup() {
        helper.close();
        YUGABYTE.stop();
    }

    @BeforeEach
    void beforeEach() {
        helper.truncateAll();
    }

    @Test
    void listWhenEmptyReturnsEmptyList() {
        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "iceberg/warehouse"));

        var expected = List.of();
        assertEquals(expected, result);
    }

    @Test
    void addThenListBySubjectAndResourceReturnsBinding() {
        var binding = new StoredRoleBinding("alice", "iceberg/warehouse", "IcebergCatalogAdmin", "allow");
        txManager.inTransaction(tx -> repository.add(tx, List.of(binding)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "iceberg/warehouse"));

        var expected = List.of(binding);
        assertEquals(expected, result);
    }

    @Test
    void addThenListByResourceReturnsAllSubjectsAtResource() {
        var binding = new StoredRoleBinding("alice", "iceberg/warehouse", "IcebergCatalogAdmin", "allow");
        txManager.inTransaction(tx -> repository.add(tx, List.of(binding)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, null, "iceberg/warehouse"));

        var expected = List.of(binding);
        assertEquals(expected, result);
    }

    @Test
    void addThenListAllReturnsAllBindings() {
        var aliceBinding = new StoredRoleBinding("alice", "iceberg/warehouse", "IcebergCatalogAdmin", "allow");
        var bobBinding = new StoredRoleBinding("bob", "paimon/events", "PaimonCatalogViewer", "allow");
        txManager.inTransaction(tx -> repository.add(tx, List.of(aliceBinding, bobBinding)));

        var result = txManager.inTransactionR(tx -> repository.listAll(tx));

        var expected = List.of(aliceBinding, bobBinding);
        assertEquals(expected, result);
    }

    @Test
    void listFiltersBySubjectAndExactResource() {
        var matching = new StoredRoleBinding("alice", "iceberg/warehouse", "IcebergCatalogAdmin", "allow");
        var otherSubject = new StoredRoleBinding("bob", "iceberg/warehouse", "IcebergCatalogAdmin", "allow");
        var otherResource = new StoredRoleBinding("alice", "paimon/events", "PaimonCatalogAdmin", "allow");
        txManager.inTransaction(tx -> repository.add(tx, List.of(matching, otherSubject, otherResource)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "iceberg/warehouse"));

        var expected = List.of(matching);
        assertEquals(expected, result);
    }

    @Test
    void listDoesNotMatchResourcePrefix() {
        var binding = new StoredRoleBinding("alice", "iceberg/warehouse", "IcebergCatalogAdmin", "allow");
        txManager.inTransaction(tx -> repository.add(tx, List.of(binding)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "iceberg/warehouse/analytics"));

        var expected = List.of();
        assertEquals(expected, result);
    }

    @Test
    void addMultipleBindingsThenListAllReturnsSorted() {
        var charlie = new StoredRoleBinding("charlie", "lance/lake", "LanceCatalogEditor", "allow");
        var alice = new StoredRoleBinding("alice", "iceberg/warehouse", "IcebergCatalogAdmin", "allow");
        var bob = new StoredRoleBinding("bob", "paimon/events", "PaimonCatalogViewer", "allow");
        txManager.inTransaction(tx -> repository.add(tx, List.of(charlie, alice, bob)));

        var result = txManager.inTransactionR(tx -> repository.listAll(tx));

        var expected = List.of(alice, bob, charlie);
        assertEquals(expected, result);
    }

    @Test
    void deleteRemovesBinding() {
        var binding = new StoredRoleBinding("alice", "iceberg/warehouse", "IcebergCatalogAdmin", "allow");
        txManager.inTransaction(tx -> repository.add(tx, List.of(binding)));
        var beforeDelete = txManager.inTransactionR(tx -> repository.list(tx, "alice", "iceberg/warehouse"));
        assertEquals(1, beforeDelete.size());

        txManager.inTransaction(tx -> repository.delete(tx, List.of(binding)));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "iceberg/warehouse"));

        var expected = List.of();
        assertEquals(expected, result);
    }

    @Test
    void deleteOnlyRemovesMatchingBindings() {
        var toDelete = new StoredRoleBinding("alice", "iceberg/warehouse", "IcebergCatalogAdmin", "allow");
        var toKeep = new StoredRoleBinding("alice", "iceberg/warehouse/analytics", "IcebergCatalogViewer", "allow");
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
        var binding = new StoredRoleBinding("alice", "iceberg/warehouse", "IcebergCatalogAdmin", "allow");
        txManager.inTransaction(tx -> repository.add(tx, List.of(binding)));
        txManager.inTransaction(tx -> repository.delete(tx, List.of()));

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "iceberg/warehouse"));

        var expected = List.of(binding);
        assertEquals(expected, result);
    }

    @Test
    void addDuplicateBindingKeepsSingleRow() {
        var binding = new StoredRoleBinding("alice", "iceberg/warehouse", "IcebergCatalogAdmin", "allow");
        txManager.inTransaction(tx -> {
            repository.add(tx, List.of(binding));
            repository.add(tx, List.of(binding));
        });

        var result = txManager.inTransactionR(tx -> repository.list(tx, "alice", "iceberg/warehouse"));

        var expected = List.of(binding);
        assertEquals(expected, result);
    }

    @Test
    void sameRoleDifferentResourcesAreDistinctRows() {
        var catalogScope = new StoredRoleBinding("alice", "iceberg/warehouse", "IcebergCatalogEditor", "allow");
        var namespaceScope = new StoredRoleBinding("alice", "iceberg/warehouse/analytics", "IcebergCatalogEditor", "allow");
        txManager.inTransaction(tx -> repository.add(tx, List.of(catalogScope, namespaceScope)));

        var catalogResult = txManager.inTransactionR(tx -> repository.list(tx, "alice", "iceberg/warehouse"));
        var namespaceResult = txManager.inTransactionR(tx -> repository.list(tx, "alice", "iceberg/warehouse/analytics"));

        assertEquals(List.of(catalogScope), catalogResult);
        assertEquals(List.of(namespaceScope), namespaceResult);
    }

    @Test
    void currentRevisionReturnsZeroWhenEmpty() {
        var revision = txManager.inTransactionR(repository::currentRevision);

        assertEquals(0L, revision);
    }

    @Test
    void bumpRevisionIncrementsRevision() {
        txManager.inTransaction(repository::bumpRevision);

        var revision = txManager.inTransactionR(repository::currentRevision);

        assertEquals(1L, revision);
    }

    @Test
    void addAndDeleteWithBumpRevisionInSameTransaction() {
        var binding = new StoredRoleBinding("alice", "iceberg/warehouse", "IcebergCatalogAdmin", "allow");
        txManager.inTransaction(tx -> {
            repository.add(tx, List.of(binding));
            repository.bumpRevision(tx);
        });
        txManager.inTransaction(tx -> {
            repository.delete(tx, List.of(binding));
            repository.bumpRevision(tx);
        });

        var revision = txManager.inTransactionR(repository::currentRevision);

        assertEquals(2L, revision);
    }

    @Test
    void addRejectsBindingWithInvalidEffect() {
        var invalid = new StoredRoleBinding("alice", "iceberg/warehouse", "IcebergCatalogEditor", "block");

        assertThrows(RuntimeException.class, () ->
                txManager.inTransaction(tx -> repository.add(tx, List.of(invalid))));
    }

    @Test
    void addRejectsBindingWithMissingEffect() {
        var invalid = new StoredRoleBinding("alice", "iceberg/warehouse", "IcebergCatalogEditor", null);

        assertThrows(RuntimeException.class, () ->
                txManager.inTransaction(tx -> repository.add(tx, List.of(invalid))));
    }
}
