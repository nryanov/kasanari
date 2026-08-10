package kasanari.repository.paimon.yugabyte;

import kasanari.fixtures.yugabyte.YugabyteFixtureContainer;
import kasanari.fixtures.yugabyte.YugabyteHelper;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.repository.paimon.model.BranchRecord;
import kasanari.repository.paimon.model.DatabaseRecord;
import kasanari.repository.paimon.model.FunctionRecord;
import kasanari.repository.paimon.model.TableRecord;
import kasanari.repository.paimon.model.TagRecord;
import kasanari.repository.paimon.model.ViewRecord;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.partition.PartitionStatistics;
import org.jdbi.v3.core.Handle;

import java.util.Map;
import java.util.Optional;

public final class JdbcPaimonYugabyteTestHelper {

    public static final String DEFAULT_CATALOG_NAME = "test-catalog";
    public static final String DEFAULT_DATABASE = "test_db";
    public static final String DEFAULT_TABLE = "test_table";
    public static final String DEFAULT_BRANCH = "main";

    private static KasanariDataSource dataSource;
    private static TransactionManager<Handle> transactionManager;

    private JdbcPaimonYugabyteTestHelper() {
    }

    public static TransactionManager<Handle> transactionManager(YugabyteFixtureContainer yugabyte) {
        if (transactionManager == null) {
            dataSource = new KasanariDataSource(yugabyte.dataSourceProperties());
            transactionManager = new JdbcTransactionManager(dataSource);
        }
        return transactionManager;
    }

    public static void initializeSchema(TransactionManager<Handle> txManager) {
        txManager.inTransaction(tx -> {
            tx.createUpdate(JdbcQueries.CREATE_DATABASES_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_TABLES_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_VIEWS_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_FUNCTIONS_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_BRANCHES_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_TAGS_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_PARTITION_STATS_DELTAS_DDL).execute();
            tx.createUpdate(JdbcQueries.CREATE_PARTITION_STATES_DDL).execute();
        });
    }

    public static void truncateAll(YugabyteHelper yugabyteHelper) {
        yugabyteHelper.truncateTable("kasanari_paimon_partition_stats_deltas");
        yugabyteHelper.truncateTable("kasanari_paimon_partition_states");
        yugabyteHelper.truncateTable("kasanari_paimon_branches");
        yugabyteHelper.truncateTable("kasanari_paimon_tags");
        yugabyteHelper.truncateTable("kasanari_paimon_tables");
        yugabyteHelper.truncateTable("kasanari_paimon_views");
        yugabyteHelper.truncateTable("kasanari_paimon_functions");
        yugabyteHelper.truncateTable("kasanari_paimon_databases");
    }

    public static void createDatabase(
            TransactionManager<Handle> txManager,
            String catalogName,
            DatabaseRecord record) {
        var repository = new JdbcDatabaseRepository(catalogName);
        txManager.inTransaction(tx -> repository.create(tx, record));
    }

    public static void createDatabase(TransactionManager<Handle> txManager, String catalogName) {
        createDatabase(txManager, catalogName, databaseRecord(DEFAULT_DATABASE));
    }

    public static void createTable(
            TransactionManager<Handle> txManager,
            String catalogName,
            TableRecord record) {
        var repository = new JdbcTableRepository(catalogName);
        txManager.inTransaction(tx -> repository.create(tx, record));
    }

    public static void createDatabaseAndTable(
            TransactionManager<Handle> txManager,
            String catalogName,
            String database,
            String table) {
        createDatabase(txManager, catalogName, databaseRecord(database));
        createTable(txManager, catalogName, tableRecord(database, table));
    }

    public static void createDatabaseAndTable(TransactionManager<Handle> txManager, String catalogName) {
        createDatabaseAndTable(txManager, catalogName, DEFAULT_DATABASE, DEFAULT_TABLE);
    }

    public static DatabaseRecord databaseRecord(String name) {
        return new DatabaseRecord(name, Map.of("owner", "test"), Optional.of("db comment"));
    }

    public static TableRecord tableRecord(String database, String table) {
        return new TableRecord(database, table, Map.of("format", "parquet"), Optional.of("uuid-" + table));
    }

    public static ViewRecord viewRecord(String database, String name) {
        return new ViewRecord(
                database,
                name,
                "SELECT 1",
                Map.of("default", "spark"),
                Map.of("owner", "test"),
                Optional.of("view comment"));
    }

    public static FunctionRecord functionRecord(String database, String name) {
        return new FunctionRecord(
                database,
                name,
                true,
                sqlDefinition("SELECT 1"),
                Optional.of("function comment"),
                Map.of("owner", "test"));
    }

    public static TagRecord tagRecord(String database, String table, String tagName) {
        return new TagRecord(database, table, tagName, 1L, Optional.of(100L), Optional.of("7d"));
    }

    public static BranchRecord branchRecord(String database, String table, String branchName) {
        return new BranchRecord(database, table, branchName, Optional.of("tag-v1"));
    }

    public static Identifier tableIdentifier(String database, String table) {
        return Identifier.create(database, table);
    }

    public static Map<String, String> partitionSpec(String key, String value) {
        return Map.of(key, value);
    }

    public static PartitionStatistics partitionStatistics(
            Map<String, String> spec,
            long recordCount,
            long fileSize,
            long fileCount,
            long lastFileCreationTime,
            int totalBuckets) {
        return new PartitionStatistics(spec, recordCount, fileSize, fileCount, lastFileCreationTime, totalBuckets);
    }

    public static Map<String, FunctionRecord.FunctionDefinition> sqlDefinition(String sql) {
        return Map.of("main", new FunctionRecord.FunctionDefinition.Sql(sql));
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
            transactionManager = null;
        }
    }
}
