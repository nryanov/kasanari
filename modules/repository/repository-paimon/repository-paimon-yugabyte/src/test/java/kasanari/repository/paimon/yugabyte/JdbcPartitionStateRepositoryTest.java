package kasanari.repository.paimon.yugabyte;

import kasanari.fixtures.yugabyte.YugabyteFixtureContainer;
import kasanari.fixtures.yugabyte.YugabyteHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.paimon.model.PartitionStateRecord;
import org.apache.paimon.catalog.Identifier;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static kasanari.repository.paimon.yugabyte.JdbcPaimonYugabyteTestHelper.DEFAULT_BRANCH;
import static kasanari.repository.paimon.yugabyte.JdbcPaimonYugabyteTestHelper.DEFAULT_CATALOG_NAME;
import static kasanari.repository.paimon.yugabyte.JdbcPaimonYugabyteTestHelper.DEFAULT_DATABASE;
import static kasanari.repository.paimon.yugabyte.JdbcPaimonYugabyteTestHelper.DEFAULT_TABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcPartitionStateRepositoryTest {

    private static final YugabyteFixtureContainer YUGABYTE = new YugabyteFixtureContainer();
    private static YugabyteHelper yugabyteHelper;
    private static TransactionManager<Handle> txManager;

    private static final Identifier TABLE = Identifier.create(DEFAULT_DATABASE, DEFAULT_TABLE);
    private static final Map<String, String> SPEC = JdbcPaimonYugabyteTestHelper.partitionSpec("dt", "2024-01-01");

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
        JdbcPaimonYugabyteTestHelper.createDatabaseAndTable(txManager, DEFAULT_CATALOG_NAME);
    }

    @AfterEach
    void afterEach() {
        JdbcPaimonYugabyteTestHelper.truncateAll(yugabyteHelper);
    }

    @Test
    void persistCommitStatisticsThenFindAllReturnsPartition() {
        var statistics = List.of(JdbcPaimonYugabyteTestHelper.partitionStatistics(SPEC, 10L, 100L, 1L, 1000L, 4));
        var repository = new JdbcPartitionStateRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.persistCommitStatistics(tx, TABLE, DEFAULT_BRANCH, 1L, statistics));

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE, DEFAULT_BRANCH));

        var expected = List.of(new PartitionStateRecord(SPEC, 10L, 100L, 1L, 1000L, 4, false, Map.of()));
        assertEquals(expected, result);
    }

    @Test
    void persistCommitStatisticsAccumulatesCounts() {
        var firstCommit = List.of(JdbcPaimonYugabyteTestHelper.partitionStatistics(SPEC, 10L, 100L, 1L, 1000L, 4));
        var secondCommit = List.of(JdbcPaimonYugabyteTestHelper.partitionStatistics(SPEC, 5L, 50L, 1L, 2000L, 4));
        var repository = new JdbcPartitionStateRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.persistCommitStatistics(tx, TABLE, DEFAULT_BRANCH, 1L, firstCommit));
        txManager.inTransaction(tx -> repository.persistCommitStatistics(tx, TABLE, DEFAULT_BRANCH, 2L, secondCommit));

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE, DEFAULT_BRANCH));

        var expected = List.of(new PartitionStateRecord(SPEC, 15L, 150L, 2L, 2000L, 4, false, Map.of()));
        assertEquals(expected, result);
    }

    @Test
    void findBySpecsReturnsMatchingPartitions() {
        var specA = JdbcPaimonYugabyteTestHelper.partitionSpec("dt", "2024-01-01");
        var specB = JdbcPaimonYugabyteTestHelper.partitionSpec("dt", "2024-01-02");
        var repository = new JdbcPartitionStateRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.persistCommitStatistics(
                tx, TABLE, DEFAULT_BRANCH, 1L,
                List.of(
                        JdbcPaimonYugabyteTestHelper.partitionStatistics(specA, 10L, 100L, 1L, 1000L, 4),
                        JdbcPaimonYugabyteTestHelper.partitionStatistics(specB, 20L, 200L, 2L, 2000L, 4))));
        txManager.inTransaction(tx -> repository.persistCommitStatistics(
                tx, TABLE, DEFAULT_BRANCH, 2L,
                List.of(JdbcPaimonYugabyteTestHelper.partitionStatistics(specB, 5L, 50L, 1L, 3000L, 4))));

        var result = txManager.inTransactionR(tx -> repository.findBySpecs(tx, TABLE, DEFAULT_BRANCH, List.of(specA)));

        var expected = List.of(new PartitionStateRecord(specA, 10L, 100L, 1L, 1000L, 4, false, Map.of()));
        assertEquals(expected, result);
    }

    @Test
    void findBySpecsEmptyListReturnsEmpty() {
        var repository = new JdbcPartitionStateRepository(DEFAULT_CATALOG_NAME);

        var result = txManager.inTransactionR(tx -> repository.findBySpecs(tx, TABLE, DEFAULT_BRANCH, List.of()));

        var expected = List.<PartitionStateRecord>of();
        assertEquals(expected, result);
    }

    @Test
    void markDoneSetsDoneFlag() {
        var statistics = List.of(JdbcPaimonYugabyteTestHelper.partitionStatistics(SPEC, 10L, 100L, 1L, 1000L, 4));
        var repository = new JdbcPartitionStateRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.persistCommitStatistics(tx, TABLE, DEFAULT_BRANCH, 1L, statistics));
        txManager.inTransaction(tx -> repository.markDone(tx, TABLE, DEFAULT_BRANCH, List.of(SPEC)));

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE, DEFAULT_BRANCH));

        var expected = List.of(new PartitionStateRecord(SPEC, 10L, 100L, 1L, 1000L, 4, true, Map.of()));
        assertEquals(expected, result);
    }

    @Test
    void createPartitionsInsertsEmptyPartition() {
        var repository = new JdbcPartitionStateRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.createPartitions(tx, TABLE, DEFAULT_BRANCH, List.of(SPEC)));

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE, DEFAULT_BRANCH));

        var expected = List.of(new PartitionStateRecord(SPEC, 0L, 0L, 0L, 0L, 0, false, Map.of()));
        assertEquals(expected, result);
    }

    @Test
    void dropPartitionsRemovesPartition() {
        var statistics = List.of(JdbcPaimonYugabyteTestHelper.partitionStatistics(SPEC, 10L, 100L, 1L, 1000L, 4));
        var repository = new JdbcPartitionStateRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.persistCommitStatistics(tx, TABLE, DEFAULT_BRANCH, 1L, statistics));
        txManager.inTransaction(tx -> repository.dropPartitions(tx, TABLE, DEFAULT_BRANCH, List.of(SPEC)));

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE, DEFAULT_BRANCH));

        var expected = List.<PartitionStateRecord>of();
        assertEquals(expected, result);
    }

    @Test
    void alterPartitionsReplacesCounts() {
        var statistics = List.of(JdbcPaimonYugabyteTestHelper.partitionStatistics(SPEC, 10L, 100L, 1L, 1000L, 4));
        var replacement = List.of(JdbcPaimonYugabyteTestHelper.partitionStatistics(SPEC, 99L, 999L, 9L, 9000L, 8));
        var repository = new JdbcPartitionStateRepository(DEFAULT_CATALOG_NAME);
        txManager.inTransaction(tx -> repository.persistCommitStatistics(tx, TABLE, DEFAULT_BRANCH, 1L, statistics));
        txManager.inTransaction(tx -> repository.alterPartitions(tx, TABLE, DEFAULT_BRANCH, replacement));

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE, DEFAULT_BRANCH));

        var expected = List.of(new PartitionStateRecord(SPEC, 99L, 999L, 9L, 9000L, 8, false, Map.of()));
        assertEquals(expected, result);
    }
}
