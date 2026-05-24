package kasanari.repository.paimon.postgres;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.fixtures.postgres.PostgresHelper;
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

import static kasanari.repository.paimon.postgres.JdbcPaimonPostgresTestHelper.DEFAULT_BRANCH;
import static kasanari.repository.paimon.postgres.JdbcPaimonPostgresTestHelper.DEFAULT_CATALOG_KEY;
import static kasanari.repository.paimon.postgres.JdbcPaimonPostgresTestHelper.DEFAULT_DATABASE;
import static kasanari.repository.paimon.postgres.JdbcPaimonPostgresTestHelper.DEFAULT_TABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JdbcPartitionStateRepositoryTest {

    private static final PostgresFixtureContainer POSTGRES = new PostgresFixtureContainer();
    private static PostgresHelper postgresHelper;
    private static TransactionManager<Handle> txManager;

    private static final Identifier TABLE = Identifier.create(DEFAULT_DATABASE, DEFAULT_TABLE);
    private static final Map<String, String> SPEC = JdbcPaimonPostgresTestHelper.partitionSpec("dt", "2024-01-01");

    @BeforeAll
    static void setup() {
        POSTGRES.start();
        postgresHelper = new PostgresHelper(POSTGRES);
    }

    @AfterAll
    static void cleanup() {
        JdbcPaimonPostgresTestHelper.close();
        POSTGRES.stop();
    }

    @BeforeEach
    void beforeEach() {
        txManager = JdbcPaimonPostgresTestHelper.transactionManager(POSTGRES);
        JdbcPaimonPostgresTestHelper.initializeSchema(txManager);
        JdbcPaimonPostgresTestHelper.createDatabaseAndTable(txManager, DEFAULT_CATALOG_KEY);
    }

    @AfterEach
    void afterEach() {
        JdbcPaimonPostgresTestHelper.truncateAll(postgresHelper);
    }

    @Test
    void persistCommitStatisticsThenFindAllReturnsPartition() {
        var statistics = List.of(JdbcPaimonPostgresTestHelper.partitionStatistics(SPEC, 10L, 100L, 1L, 1000L, 4));
        var repository = new JdbcPartitionStateRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.persistCommitStatistics(tx, TABLE, DEFAULT_BRANCH, 1L, statistics));

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE, DEFAULT_BRANCH));

        var expected = List.of(new PartitionStateRecord(SPEC, 10L, 100L, 1L, 1000L, 4, false, Map.of()));
        assertEquals(expected, result);
    }

    @Test
    void persistCommitStatisticsAccumulatesCounts() {
        var firstCommit = List.of(JdbcPaimonPostgresTestHelper.partitionStatistics(SPEC, 10L, 100L, 1L, 1000L, 4));
        var secondCommit = List.of(JdbcPaimonPostgresTestHelper.partitionStatistics(SPEC, 5L, 50L, 1L, 2000L, 4));
        var repository = new JdbcPartitionStateRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.persistCommitStatistics(tx, TABLE, DEFAULT_BRANCH, 1L, firstCommit));
        txManager.inTransaction(tx -> repository.persistCommitStatistics(tx, TABLE, DEFAULT_BRANCH, 2L, secondCommit));

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE, DEFAULT_BRANCH));

        var expected = List.of(new PartitionStateRecord(SPEC, 15L, 150L, 2L, 2000L, 4, false, Map.of()));
        assertEquals(expected, result);
    }

    @Test
    void findBySpecsReturnsMatchingPartitions() {
        var specA = JdbcPaimonPostgresTestHelper.partitionSpec("dt", "2024-01-01");
        var specB = JdbcPaimonPostgresTestHelper.partitionSpec("dt", "2024-01-02");
        var repository = new JdbcPartitionStateRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.persistCommitStatistics(
                tx, TABLE, DEFAULT_BRANCH, 1L,
                List.of(
                        JdbcPaimonPostgresTestHelper.partitionStatistics(specA, 10L, 100L, 1L, 1000L, 4),
                        JdbcPaimonPostgresTestHelper.partitionStatistics(specB, 20L, 200L, 2L, 2000L, 4))));
        txManager.inTransaction(tx -> repository.persistCommitStatistics(
                tx, TABLE, DEFAULT_BRANCH, 2L,
                List.of(JdbcPaimonPostgresTestHelper.partitionStatistics(specB, 5L, 50L, 1L, 3000L, 4))));

        var result = txManager.inTransactionR(tx -> repository.findBySpecs(tx, TABLE, DEFAULT_BRANCH, List.of(specA)));

        var expected = List.of(new PartitionStateRecord(specA, 10L, 100L, 1L, 1000L, 4, false, Map.of()));
        assertEquals(expected, result);
    }

    @Test
    void findBySpecsEmptyListReturnsEmpty() {
        var repository = new JdbcPartitionStateRepository(DEFAULT_CATALOG_KEY);

        var result = txManager.inTransactionR(tx -> repository.findBySpecs(tx, TABLE, DEFAULT_BRANCH, List.of()));

        var expected = List.<PartitionStateRecord>of();
        assertEquals(expected, result);
    }

    @Test
    void markDoneSetsDoneFlag() {
        var statistics = List.of(JdbcPaimonPostgresTestHelper.partitionStatistics(SPEC, 10L, 100L, 1L, 1000L, 4));
        var repository = new JdbcPartitionStateRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.persistCommitStatistics(tx, TABLE, DEFAULT_BRANCH, 1L, statistics));
        txManager.inTransaction(tx -> repository.markDone(tx, TABLE, DEFAULT_BRANCH, List.of(SPEC)));

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE, DEFAULT_BRANCH));

        var expected = List.of(new PartitionStateRecord(SPEC, 10L, 100L, 1L, 1000L, 4, true, Map.of()));
        assertEquals(expected, result);
    }

    @Test
    void createPartitionsInsertsEmptyPartition() {
        var repository = new JdbcPartitionStateRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.createPartitions(tx, TABLE, DEFAULT_BRANCH, List.of(SPEC)));

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE, DEFAULT_BRANCH));

        var expected = List.of(new PartitionStateRecord(SPEC, 0L, 0L, 0L, 0L, 0, false, Map.of()));
        assertEquals(expected, result);
    }

    @Test
    void dropPartitionsRemovesPartition() {
        var statistics = List.of(JdbcPaimonPostgresTestHelper.partitionStatistics(SPEC, 10L, 100L, 1L, 1000L, 4));
        var repository = new JdbcPartitionStateRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.persistCommitStatistics(tx, TABLE, DEFAULT_BRANCH, 1L, statistics));
        txManager.inTransaction(tx -> repository.dropPartitions(tx, TABLE, DEFAULT_BRANCH, List.of(SPEC)));

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE, DEFAULT_BRANCH));

        var expected = List.<PartitionStateRecord>of();
        assertEquals(expected, result);
    }

    @Test
    void alterPartitionsReplacesCounts() {
        var statistics = List.of(JdbcPaimonPostgresTestHelper.partitionStatistics(SPEC, 10L, 100L, 1L, 1000L, 4));
        var replacement = List.of(JdbcPaimonPostgresTestHelper.partitionStatistics(SPEC, 99L, 999L, 9L, 9000L, 8));
        var repository = new JdbcPartitionStateRepository(DEFAULT_CATALOG_KEY);
        txManager.inTransaction(tx -> repository.persistCommitStatistics(tx, TABLE, DEFAULT_BRANCH, 1L, statistics));
        txManager.inTransaction(tx -> repository.alterPartitions(tx, TABLE, DEFAULT_BRANCH, replacement));

        var result = txManager.inTransactionR(tx -> repository.findAll(tx, TABLE, DEFAULT_BRANCH));

        var expected = List.of(new PartitionStateRecord(SPEC, 99L, 999L, 9L, 9000L, 8, false, Map.of()));
        assertEquals(expected, result);
    }
}
