package kasanari.repository.paimon.postgres;

import kasanari.fixtures.postgres.PostgresFixtureContainer;
import kasanari.repository.paimon.model.DatabaseRecord;
import kasanari.repository.paimon.model.FunctionRecord;
import kasanari.repository.paimon.model.TableRecord;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.partition.PartitionStatistics;
import org.jdbi.v3.core.Jdbi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

final class PaimonPostgresTestHelper {
    static final String CATALOG_KEY = "test-catalog";
    static final String DATABASE = "test_db";
    static final String TABLE = "test_table";

    private final PostgresFixtureContainer postgres;
    private Jdbi jdbi;
    private boolean schemaInitialized;

    PaimonPostgresTestHelper(PostgresFixtureContainer postgres) {
        this.postgres = postgres;
    }

    String catalogKey() {
        return CATALOG_KEY;
    }

    Jdbi jdbi() {
        return jdbi;
    }

    void initializeSchemaIfNeeded() {
        if (schemaInitialized) {
            return;
        }
        jdbi = Jdbi.create(postgres.jdbcUrl(), postgres.username(), postgres.password());
        var properties = new LinkedHashMap<String, String>();
        properties.put("uri", postgres.jdbcUrl());
        properties.put("user", postgres.username());
        properties.put("password", postgres.password());
        new JdbcTableInitializer(new kasanari.repository.jdbc.KasanariDataSource(properties)).initialize();
        schemaInitialized = true;
    }

    void truncateAll() {
        jdbi.useHandle(handle -> handle.createUpdate("""
                TRUNCATE TABLE
                    kasanari_paimon_partition_stats_deltas,
                    kasanari_paimon_partition_states,
                    kasanari_paimon_branches,
                    kasanari_paimon_tags,
                    kasanari_paimon_tables,
                    kasanari_paimon_views,
                    kasanari_paimon_functions,
                    kasanari_paimon_databases
                """).execute());
    }

    void inTransaction(Runnable action) {
        jdbi.useTransaction(handle -> {
            action.run();
            return null;
        });
    }

    <T> T inTransaction(Function<org.jdbi.v3.core.Handle, T> action) {
        return jdbi.inTransaction(action);
    }

    void createDatabase(DatabaseRecord record) {
        inTransaction(tx -> new JdbcDatabaseRepository(CATALOG_KEY).create(tx, record));
    }

    void createDatabase() {
        createDatabase(new DatabaseRecord(DATABASE, Map.of("owner", "test"), Optional.of("db comment")));
    }

    void createTable(TableRecord record) {
        inTransaction(tx -> new JdbcTableRepository(CATALOG_KEY).create(tx, record));
    }

    void createDatabaseAndTable(String tableName) {
        createDatabase();
        createTable(new TableRecord(DATABASE, tableName, Map.of("format", "parquet"), Optional.of("uuid-" + tableName)));
    }

    void createDatabaseAndTable() {
        createDatabaseAndTable(TABLE);
    }

    Identifier tableIdentifier(String tableName) {
        return Identifier.create(DATABASE, tableName);
    }

    Identifier tableIdentifier() {
        return tableIdentifier(TABLE);
    }

    Identifier viewIdentifier(String viewName) {
        return Identifier.create(DATABASE, viewName);
    }

    Identifier functionIdentifier(String functionName) {
        return Identifier.create(DATABASE, functionName);
    }

    static Map<String, String> options(String key, String value) {
        return Map.of(key, value);
    }

    static PartitionStatistics partitionStatistics(Map<String, String> spec, long recordCount, long fileSize, long fileCount, long lastFileCreationTime, int totalBuckets) {
        return new PartitionStatistics(spec, recordCount, fileSize, fileCount, lastFileCreationTime, totalBuckets);
    }

    static Map<String, FunctionRecord.FunctionDefinition> sqlDefinition(String sql) {
        return Map.of("main", new FunctionRecord.FunctionDefinition.Sql(sql));
    }
}
