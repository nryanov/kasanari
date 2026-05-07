package kasanari.catalog.paimon;

import org.apache.paimon.Snapshot;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.consumer.ConsumerInfo;
import org.apache.paimon.data.BinaryString;
import org.apache.paimon.data.GenericRow;
import org.apache.paimon.function.FunctionDefinition;
import org.apache.paimon.partition.PartitionStatistics;
import org.apache.paimon.rest.requests.AlterDatabaseRequest;
import org.apache.paimon.rest.requests.AlterFunctionRequest;
import org.apache.paimon.rest.requests.AlterTableRequest;
import org.apache.paimon.rest.requests.AlterViewRequest;
import org.apache.paimon.rest.requests.AuthTableQueryRequest;
import org.apache.paimon.rest.requests.CreateBranchRequest;
import org.apache.paimon.rest.requests.CreateDatabaseRequest;
import org.apache.paimon.rest.requests.CreateFunctionRequest;
import org.apache.paimon.rest.requests.CreateTableRequest;
import org.apache.paimon.rest.requests.CreateTagRequest;
import org.apache.paimon.rest.requests.CreateViewRequest;
import org.apache.paimon.rest.requests.ForwardBranchRequest;
import org.apache.paimon.rest.requests.ListPartitionsByNamesRequest;
import org.apache.paimon.rest.requests.MarkDonePartitionsRequest;
import org.apache.paimon.rest.requests.RegisterTableRequest;
import org.apache.paimon.rest.requests.RenameBranchRequest;
import org.apache.paimon.rest.requests.RenameTableRequest;
import org.apache.paimon.rest.requests.ResetConsumerRequest;
import org.apache.paimon.rest.requests.RollbackSchemaRequest;
import org.apache.paimon.rest.requests.RollbackTableRequest;
import org.apache.paimon.rest.responses.GetFunctionResponse;
import org.apache.paimon.rest.responses.GetViewResponse;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.table.Instant;
import org.apache.paimon.types.DataField;
import org.apache.paimon.types.DataTypes;
import org.apache.paimon.view.ViewSchema;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class PaimonCatalogAdapterTest {
    protected PaimonCatalogAdapter catalog;
    protected String prefix;
    protected String database;
    protected String table;
    protected String renamedTable;
    protected String view;
    protected String renamedView;
    protected String function;
    protected String branch;
    protected String renamedBranch;
    protected String tag;

    @BeforeAll
    public final void setup() {
        this.catalog = setupCatalogAdapter();
    }

    @BeforeEach
    public final void beforeEach() {
        reset();
        prefix = "";
        database = uniqueName("db");
        table = uniqueName("table");
        renamedTable = uniqueName("table_renamed");
        view = uniqueName("view");
        renamedView = uniqueName("view_renamed");
        function = uniqueName("fn");
        branch = uniqueName("branch");
        renamedBranch = uniqueName("branch_renamed");
        tag = uniqueName("tag");
    }

    @AfterAll
    public final void afterAll() {
        close();
    }

    protected abstract PaimonCatalogAdapter setupCatalogAdapter();

    protected void reset() {
        // Optional for concrete implementations.
    }

    protected void close() {
        // Optional for concrete implementations.
    }

    protected boolean supportsDatabases() {
        return true;
    }

    protected boolean supportsTables() {
        return true;
    }

    protected boolean supportsTableMutations() {
        return true;
    }

    protected boolean supportsPartitions() {
        return true;
    }

    protected boolean supportsBranches() {
        return true;
    }

    protected boolean supportsTags() {
        return true;
    }

    protected boolean supportsConsumers() {
        return true;
    }

    protected boolean supportsViews() {
        return true;
    }

    protected boolean supportsFunctions() {
        return true;
    }

    protected boolean supportListGlobally() {
        return true;
    }

    protected boolean supportRollbackTable() {
        return true;
    }

    protected boolean supportRollbackSchema() {
        return true;
    }

    protected boolean supportAuthTable() {
        return true;
    }

    protected boolean supportRegisterTable() {
        return true;
    }

    protected boolean supportAlterDatabase() {
        return true;
    }

    protected boolean supportAlterTable() {
        return true;
    }

    protected boolean supportAlterView() {
        return true;
    }

    protected boolean supportCommit() {
        return true;
    }

    protected boolean supportSnapshot() {
        return true;
    }

    protected boolean supportsGetTableByIdWithNamespace() {
        return true;
    }

    protected boolean supportsGetTableByIdWithoutNamespace() {
        return true;
    }

    protected boolean supportsGetTableToken() {
        return false;
    }

    protected String registeredTablePath(String database, String table) {
        return "/tmp/" + database + "/" + table;
    }

    protected String existingTableIdWithNamespace() {
        return null;
    }

    protected String existingTableIdWithoutNamespace() {
        return null;
    }

    @Test
    void listDatabases() {
        assumeTrue(supportsDatabases());

        var request = new CreateDatabaseRequest(database, Collections.emptyMap());
        catalog.createDatabase(prefix, request);

        assertTrue(catalog.listDatabases(prefix, 100, null).getDatabases().contains(database));
    }

    @Test
    void createDatabase() {
        assumeTrue(supportsDatabases());

        var request = new CreateDatabaseRequest(database, Map.of("owner", "test"));
        catalog.createDatabase(prefix, request);

        var created = catalog.getDatabase(prefix, database);

        assertEquals(database, created.getName());

        if (created.getOptions().containsKey("owner")) {
            assertEquals("test", created.getOptions().get("owner"));
        }
    }

    @Test
    void getDatabase() {
        assumeTrue(supportsDatabases());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        catalog.createDatabase(prefix, createDatabaseRequest);

        var expected = database;
        var result = catalog.getDatabase(prefix, database).getName();

        assertEquals(expected, result);
    }

    @Test
    void dropDatabase() {
        assumeTrue(supportsDatabases());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.dropDatabase(prefix, database);

        assertFalse(catalog.listDatabases(prefix, 100, null).getDatabases().contains(database));
    }

    @Test
    void alterDatabase() {
        assumeTrue(supportsDatabases() && supportAlterDatabase());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var alterDatabaseRequest = new AlterDatabaseRequest(List.of("owner"), Map.of("owner", "updated"));
        catalog.createDatabase(prefix, createDatabaseRequest);

        var result = catalog.alterDatabase(prefix, database, alterDatabaseRequest);
        assertTrue(result.getUpdated().contains("owner"));
    }

    @Test
    void alterDatabaseWithNullCollections() {
        assumeTrue(supportsDatabases() && supportAlterDatabase());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var alterDatabaseRequest = new AlterDatabaseRequest(null, null);
        catalog.createDatabase(prefix, createDatabaseRequest);

        var expected = Collections.emptyList();
        var result = catalog.alterDatabase(prefix, database, alterDatabaseRequest).getUpdated();

        assertEquals(expected, result);
    }

    @Test
    void registerTable() {
        assumeTrue(supportsDatabases() && supportsTables() && supportRegisterTable());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var registerTableRequest = new RegisterTableRequest(Identifier.create(database, table), registeredTablePath(database, table));
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.registerTable(prefix, database, registerTableRequest);

        assertEquals(table, catalog.getTable(prefix, database, table).getName());
    }

    @Test
    void listTables() {
        assumeTrue(supportsDatabases() && supportsTables());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);

        assertTrue(catalog.listTables(prefix, database, 100, null, null).getTables().contains(table));
    }

    @Test
    void createTable() {
        assumeTrue(supportsDatabases() && supportsTables());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);

        assertEquals(table, catalog.getTable(prefix, database, table).getName());
    }

    @Test
    void listTableDetails() {
        assumeTrue(supportsDatabases() && supportsTables());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);

        var result = catalog.listTableDetails(prefix, database, 100, null, null, null)
                .getTableDetails()
                .stream()
                .anyMatch(it -> table.equals(it.getName()));

        assertTrue(result);
    }

    @Test
    void listTablesGlobally() {
        assumeTrue(supportsDatabases() && supportsTables() && supportListGlobally());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);

        var result = catalog.listTablesGlobally(prefix, database, table, 100, null)
                .getTables()
                .contains(Identifier.create(database, table));

        assertTrue(result);
    }

    @Test
    void getTableByIdWithNamespace() {
        assumeTrue(supportsTables() && supportsGetTableByIdWithNamespace());
        var tableId = existingTableIdWithNamespace();
        assumeTrue(tableId != null && !tableId.isBlank());

        var expected = tableId;
        var result = catalog.getTableById(prefix, tableId).getId();

        assertEquals(expected, result);
    }

    @Test
    void getTableByIdWithoutNamespace() {
        assumeTrue(supportsTables() && supportsGetTableByIdWithoutNamespace());
        var tableId = existingTableIdWithoutNamespace();
        assumeTrue(tableId != null && !tableId.isBlank());

        var expected = tableId;
        var result = catalog.getTableById(prefix, tableId).getId();

        assertEquals(expected, result);
    }

    @Test
    void getTable() {
        assumeTrue(supportsDatabases() && supportsTables());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);

        var expected = table;
        var result = catalog.getTable(prefix, database, table).getName();

        assertEquals(expected, result);
    }

    @Test
    void alterTable() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTableMutations() && supportAlterTable());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var alterTableRequest = new AlterTableRequest(Collections.emptyList());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);
        catalog.alterTable(prefix, database, table, alterTableRequest);

        assertEquals(table, catalog.getTable(prefix, database, table).getName());
    }

    @Test
    void alterTableWithNullChanges() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTableMutations() && supportAlterTable());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var alterTableRequest = new AlterTableRequest(null);
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);
        catalog.alterTable(prefix, database, table, alterTableRequest);

        assertEquals(table, catalog.getTable(prefix, database, table).getName());
    }

    @Test
    void dropTable() {
        assumeTrue(supportsDatabases() && supportsTables());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);
        catalog.dropTable(prefix, database, table);

        assertFalse(catalog.listTables(prefix, database, 100, null, null).getTables().contains(table));
    }

    @Test
    void renameTable() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTableMutations());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var renameTableRequest = new RenameTableRequest(Identifier.create(database, table), Identifier.create(database, renamedTable));
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);
        catalog.renameTable(prefix, renameTableRequest);

        var tables = catalog.listTables(prefix, database, 100, null, null).getTables();
        assertFalse(tables.contains(table));
        assertTrue(tables.contains(renamedTable));
    }

    @Test
    void commitTable() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTableMutations() && supportCommit());

        createDatabaseAndTable();

        // commitTable (commitSnapshot) is triggered internally via table.commit operation
        var latestSnapshotBeforeCommit = catalog.getVersionSnapshot(prefix, database, table, "LATEST").getSnapshot();
        triggerTableSnapshot(database, table);
        var latestSnapshotAfterCommit = catalog.getVersionSnapshot(prefix, database, table, "LATEST").getSnapshot();

        assertNull(latestSnapshotBeforeCommit);
        assertNotNull(latestSnapshotAfterCommit);
    }

    @Test
    void rollbackTable() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTableMutations() && supportRollbackTable());

        createDatabaseAndTable();
        triggerTableSnapshot(database, table);
        var snapshotId = catalog.getVersionSnapshot(prefix, database, table, "LATEST").getSnapshot().id();
        var rollbackTableRequest = new RollbackTableRequest(Instant.snapshot(snapshotId), null);
        catalog.rollbackTable(prefix, database, table, rollbackTableRequest);

        assertEquals(snapshotId, catalog.getVersionSnapshot(prefix, database, table, "LATEST").getSnapshot().id());
    }

    @Test
    void rollbackSchema() {
        // TODO: fix
        assumeTrue(supportsDatabases() && supportsTables() && supportsTableMutations() && supportRollbackSchema());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var rollbackSchemaRequest = new RollbackSchemaRequest(1L);
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);
        catalog.rollbackSchema(prefix, database, table, rollbackSchemaRequest);

        assertEquals(table, catalog.getTable(prefix, database, table).getName());
    }

    @Test
    void getTableTokenUnsupported() {
        assumeTrue(!supportsGetTableToken());
        // TODO
    }

    @Test
    void authTableQuery() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTableMutations() && supportAuthTable());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var authTableQueryRequest = new AuthTableQueryRequest(null);
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);

        assertNotNull(catalog.authTableQuery(prefix, database, table, authTableQueryRequest));
    }

    @Test
    void getTableSnapshot() {
        assumeTrue(supportsDatabases() && supportsTables() && supportSnapshot());

        createDatabaseAndTable();
        triggerTableSnapshot(database, table);
        var latestSnapshot = catalog.getVersionSnapshot(prefix, database, table, "LATEST").getSnapshot();
        var result = catalog.getTableSnapshot(prefix, database, table).getSnapshot();

        assertNotNull(result);
        assertEquals(latestSnapshot.id(), result.snapshot().id());
    }

    @Test
    void getVersionSnapshot() {
        assumeTrue(supportsDatabases() && supportsTables() && supportSnapshot());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);
        triggerTableSnapshot(database, table);
        var latestSnapshot = catalog.getVersionSnapshot(prefix, database, table, "LATEST").getSnapshot();
        var result = catalog.getVersionSnapshot(prefix, database, table, "LATEST").getSnapshot();

        assertNotNull(result);
        assertEquals(latestSnapshot.id(), result.id());
    }

    @Test
    void listSnapshots() {
        assumeTrue(supportsDatabases() && supportsTables() && supportSnapshot());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);
        triggerTableSnapshot(database, table);

        var expected = List.of(catalog.getVersionSnapshot(prefix, database, table, "LATEST").getSnapshot().id());
        var result = catalog.listSnapshots(prefix, database, table, 100, null).getSnapshots();
        var resultIds = result.stream().map(Snapshot::id).toList();

        assertEquals(expected, resultIds);
    }

    @Test
    void listPartitions() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsPartitions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), partitionedTableSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);
        triggerPartitionedTableSnapshot(database, table, 1, "dt_a");
        triggerPartitionedTableSnapshot(database, table, 2, "dt_b");

        var result = catalog.listPartitions(prefix, database, table, 100, null, null).getPartitions();
        var specs = result.stream().map(PartitionStatistics::spec).toList();

        assertEquals(2, result.size());
        assertTrue(specs.contains(Map.of("dt", "dt_a")));
        assertTrue(specs.contains(Map.of("dt", "dt_b")));
    }

    @Test
    void markDonePartitions() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsPartitions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), partitionedTableSchema());
        var markDonePartitionsRequest = new MarkDonePartitionsRequest(List.of(Map.of("dt", "dt_a")));
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);
        triggerPartitionedTableSnapshot(database, table, 1, "dt_a");
        catalog.markDonePartitions(prefix, database, table, markDonePartitionsRequest);
        var result = catalog.listPartitions(prefix, database, table, 100, null, null).getPartitions();
        var partition = result.stream()
                .filter(it -> Map.of("dt", "dt_a").equals(it.spec()))
                .findFirst()
                .orElse(null);

        assertNotNull(partition);
        assertTrue(partition.done());
    }

    @Test
    void listPartitionsByNames() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsPartitions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), partitionedTableSchema());
        var listPartitionsByNamesRequest = new ListPartitionsByNamesRequest(List.of(Map.of("dt", "dt_a")));
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);
        triggerPartitionedTableSnapshot(database, table, 1, "dt_a");
        triggerPartitionedTableSnapshot(database, table, 2, "dt_b");

        var result = catalog.listPartitionsByNames(prefix, database, table, listPartitionsByNamesRequest).getPartitions();
        var specs = result.stream().map(PartitionStatistics::spec).toList();

        assertEquals(1, result.size());
        assertEquals(List.of(Map.of("dt", "dt_a")), specs);
    }

    @Test
    void createPartitions() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsPartitions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), partitionedTableSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);

        try {
            catalog.getUnderlyingCatalog().createPartitions(
                    Identifier.create(database, table),
                    List.of(Map.of("dt", "dt_created"))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var partitions = catalog.listPartitions(prefix, database, table, 100, null, null).getPartitions();
        var specs = partitions.stream().map(PartitionStatistics::spec).toList();
        assertTrue(specs.contains(Map.of("dt", "dt_created")));
    }

    @Test
    void dropPartitions() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsPartitions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), partitionedTableSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);

        try {
            var tableIdentifier = Identifier.create(database, table);
            catalog.getUnderlyingCatalog().createPartitions(
                    tableIdentifier,
                    List.of(Map.of("dt", "dt_drop"))
            );
            catalog.getUnderlyingCatalog().dropPartitions(
                    tableIdentifier,
                    List.of(Map.of("dt", "dt_drop"))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var partitions = catalog.listPartitions(prefix, database, table, 100, null, null).getPartitions();
        var specs = partitions.stream().map(PartitionStatistics::spec).toList();
        assertFalse(specs.contains(Map.of("dt", "dt_drop")));
    }

    @Test
    void alterPartitions() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsPartitions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), partitionedTableSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);

        try {
            catalog.getUnderlyingCatalog().alterPartitions(
                    Identifier.create(database, table),
                    List.of(new PartitionStatistics(
                            Map.of("dt", "dt_altered"),
                            7L,
                            70L,
                            3L,
                            123L,
                            1
                    ))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var partitions = catalog.listPartitions(prefix, database, table, 100, null, null).getPartitions();
        var altered = partitions.stream()
                .filter(partition -> Map.of("dt", "dt_altered").equals(partition.spec()))
                .findFirst()
                .orElse(null);
        assertNotNull(altered);
        assertEquals(7L, altered.recordCount());
        assertEquals(70L, altered.fileSizeInBytes());
        assertEquals(3L, altered.fileCount());
        assertEquals(123L, altered.lastFileCreationTime());
        assertEquals(1, altered.totalBuckets());
    }

    @Test
    void listBranches() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsBranches());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);

        var expected = List.of("main");
        var result = catalog.listBranches(prefix, database, table).branches();

        assertEquals(expected, result);
    }

    @Test
    void createBranch() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsBranches());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var createBranchRequest = new CreateBranchRequest(branch, null);
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);
        catalog.createBranch(prefix, database, table, createBranchRequest);
        var branches = catalog.listBranches(prefix, database, table).branches();

        assertTrue(branches.contains(branch));
    }

    @Test
    void dropBranch() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsBranches());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var createBranchRequest = new CreateBranchRequest(branch, null);
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);
        catalog.createBranch(prefix, database, table, createBranchRequest);
        catalog.dropBranch(prefix, database, table, branch);
        var branches = catalog.listBranches(prefix, database, table).branches();

        assertFalse(branches.contains(branch));
    }

    @Test
    void renameBranch() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsBranches());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var createBranchRequest = new CreateBranchRequest(branch, null);
        var renameBranchRequest = new RenameBranchRequest(renamedBranch);
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);
        catalog.createBranch(prefix, database, table, createBranchRequest);
        catalog.renameBranch(prefix, database, table, branch, renameBranchRequest);
        var branches = catalog.listBranches(prefix, database, table).branches();

        assertFalse(branches.contains(branch));
        assertTrue(branches.contains(renamedBranch));
    }

    @Test
    void forwardBranch() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsBranches());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var createBranchRequest = new CreateBranchRequest(branch, null);
        var forwardBranchRequest = new ForwardBranchRequest();

        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);
        triggerTableSnapshot(database, table);

        catalog.createBranch(prefix, database, table, createBranchRequest);
        triggerTableSnapshotInBranch(database, table, branch);

        catalog.forwardBranch(prefix, database, table, branch, forwardBranchRequest);

        var branches = catalog.listBranches(prefix, database, table).branches();

        assertTrue(branches.contains(branch));
    }

    @Test
    void listTags() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTags());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);

        var expected = Collections.emptyList();
        var result = catalog.listTags(prefix, database, table, 100, null, null).tags();

        assertEquals(expected, result);
    }

    @Test
    void createTag() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTags());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var createTagRequest = new CreateTagRequest(tag, null, null);
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);
        triggerTableSnapshot(database, table);
        catalog.createTag(prefix, database, table, createTagRequest);

        assertEquals(tag, catalog.getTag(prefix, database, table, tag).tagName());
    }

    @Test
    void getTag() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTags());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var createTagRequest = new CreateTagRequest(tag, null, null);
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);
        triggerTableSnapshot(database, table);
        catalog.createTag(prefix, database, table, createTagRequest);

        var expected = tag;
        var result = catalog.getTag(prefix, database, table, tag).tagName();

        assertEquals(expected, result);
    }

    @Test
    void deleteTag() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTags());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var createTagRequest = new CreateTagRequest(tag, null, null);
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);
        triggerTableSnapshot(database, table);
        catalog.createTag(prefix, database, table, createTagRequest);
        catalog.deleteTag(prefix, database, table, tag);

        assertEquals(Collections.emptyList(), catalog.listTags(prefix, database, table, 100, null, null).tags());
    }

    @Test
    void listConsumers() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsConsumers());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);

        var expected = Collections.emptyList();
        var result = catalog.listConsumers(prefix, database, table, 100, null).getConsumers();

        assertEquals(expected, result);
    }

    @Test
    void resetConsumer() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsConsumers());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());

        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createTable(prefix, database, createTableRequest);

        triggerTableSnapshot(database, table);
        var snapshot = catalog.getTableSnapshot(prefix, database, table);
        var snapshotId = snapshot.getSnapshot().snapshot().id();
        var resetConsumerRequest = new ResetConsumerRequest(uniqueName("consumer"), snapshotId);

        catalog.resetConsumer(prefix, database, table, resetConsumerRequest);

        var consumers = catalog.listConsumers(prefix, database, table, 100, null).getConsumers();
        var consumerIds = consumers.stream().map(ConsumerInfo::getConsumerId).toList();

        assertEquals(List.of(resetConsumerRequest.consumerId()), consumerIds);
    }

    @Test
    void listViews() {
        assumeTrue(supportsDatabases() && supportsViews());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createViewRequest = new CreateViewRequest(Identifier.create(database, view), viewSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createView(prefix, database, createViewRequest);

        var expected = List.of(view);
        var result = catalog.listViews(prefix, database, 100, null, null).getViews().stream()
                .filter(expected::contains)
                .toList();

        assertEquals(expected, result);
    }

    @Test
    void createView() {
        assumeTrue(supportsDatabases() && supportsViews());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createViewRequest = new CreateViewRequest(Identifier.create(database, view), viewSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createView(prefix, database, createViewRequest);

        assertEquals(view, catalog.getView(prefix, database, view).getName());
    }

    @Test
    void listViewDetails() {
        assumeTrue(supportsDatabases() && supportsViews());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createViewRequest = new CreateViewRequest(Identifier.create(database, view), viewSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createView(prefix, database, createViewRequest);

        var expected = List.of(view);
        var result = catalog.listViewDetails(prefix, database, 100, null, null)
                .getViewDetails()
                .stream()
                .map(GetViewResponse::getName)
                .filter(expected::contains)
                .toList();

        assertEquals(expected, result);
    }

    @Test
    void listViewsGlobally() {
        assumeTrue(supportsDatabases() && supportsViews() && supportListGlobally());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createViewRequest = new CreateViewRequest(Identifier.create(database, view), viewSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createView(prefix, database, createViewRequest);

        var expected = List.of(Identifier.create(database, view));
        var result = catalog.listViewsGlobally(prefix, database, view, 100, null).getViews();

        assertEquals(expected, result);
    }

    @Test
    void getView() {
        assumeTrue(supportsDatabases() && supportsViews());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createViewRequest = new CreateViewRequest(Identifier.create(database, view), viewSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createView(prefix, database, createViewRequest);

        var expected = view;
        var result = catalog.getView(prefix, database, view).getName();

        assertEquals(expected, result);
    }

    @Test
    void alterView() {
        assumeTrue(supportsDatabases() && supportsViews() && supportAlterView());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createViewRequest = new CreateViewRequest(Identifier.create(database, view), viewSchema());
        var alterViewRequest = new AlterViewRequest(Collections.emptyList());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createView(prefix, database, createViewRequest);
        catalog.alterView(prefix, database, view, alterViewRequest);

        assertEquals(view, catalog.getView(prefix, database, view).getName());
    }

    @Test
    void dropView() {
        assumeTrue(supportsDatabases() && supportsViews());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createViewRequest = new CreateViewRequest(Identifier.create(database, view), viewSchema());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createView(prefix, database, createViewRequest);
        catalog.dropView(prefix, database, view);

        assertEquals(Collections.emptyList(), catalog.listViews(prefix, database, 100, null, null).getViews());
    }

    @Test
    void renameView() {
        assumeTrue(supportsDatabases() && supportsViews());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createViewRequest = new CreateViewRequest(Identifier.create(database, view), viewSchema());
        var renameViewRequest = new RenameTableRequest(Identifier.create(database, view), Identifier.create(database, renamedView));
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createView(prefix, database, createViewRequest);
        catalog.renameView(prefix, renameViewRequest);
        var views = catalog.listViews(prefix, database, 100, null, null).getViews();

        assertFalse(views.contains(view));
        assertTrue(views.contains(renamedView));
    }

    @Test
    void listFunctions() {
        assumeTrue(supportsDatabases() && supportsFunctions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createFunctionRequest = functionRequest(function);
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createFunction(prefix, database, createFunctionRequest);

        var expected = List.of(function);
        var result = catalog.listFunctions(prefix, database, 100, null, null).functions().stream()
                .filter(expected::contains)
                .toList();

        assertEquals(expected, result);
    }

    @Test
    void createFunction() {
        assumeTrue(supportsDatabases() && supportsFunctions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createFunctionRequest = functionRequest(function);
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createFunction(prefix, database, createFunctionRequest);

        assertEquals(function, catalog.getFunction(prefix, database, function).name());
    }

    @Test
    void listFunctionDetails() {
        assumeTrue(supportsDatabases() && supportsFunctions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createFunctionRequest = functionRequest(function);

        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createFunction(prefix, database, createFunctionRequest);

        var expected = List.of(function);
        var result = catalog.listFunctionDetails(prefix, database, 100, null, null)
                .getFunctionDetails()
                .stream()
                .map(GetFunctionResponse::name)
                .toList();

        assertEquals(expected, result);
    }

    @Test
    void listFunctionsGlobally() {
        assumeTrue(supportsDatabases() && supportsFunctions() && supportListGlobally());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createFunctionRequest = functionRequest(function);
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createFunction(prefix, database, createFunctionRequest);

        var expected = List.of(Identifier.create(database, function));
        var result = catalog.listFunctionsGlobally(prefix, database, function, 100, null).functions();

        assertEquals(expected, result);
    }

    @Test
    void getFunction() {
        assumeTrue(supportsDatabases() && supportsFunctions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createFunctionRequest = functionRequest(function);
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createFunction(prefix, database, createFunctionRequest);

        var expected = function;
        var result = catalog.getFunction(prefix, database, function).name();

        assertEquals(expected, result);
    }

    @Test
    void alterFunction() {
        assumeTrue(supportsDatabases() && supportsFunctions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createFunctionRequest = functionRequest(function);
        var alterFunctionRequest = new AlterFunctionRequest(Collections.emptyList());
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createFunction(prefix, database, createFunctionRequest);
        catalog.alterFunction(prefix, database, function, alterFunctionRequest);

        assertEquals(function, catalog.getFunction(prefix, database, function).name());
    }

    @Test
    void dropFunction() {
        assumeTrue(supportsDatabases() && supportsFunctions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createFunctionRequest = functionRequest(function);
        catalog.createDatabase(prefix, createDatabaseRequest);
        catalog.createFunction(prefix, database, createFunctionRequest);
        catalog.dropFunction(prefix, database, function);

        assertEquals(Collections.emptyList(), catalog.listFunctions(prefix, database, 100, null, null).functions());
    }

    protected CreateFunctionRequest functionRequest(String name) {
        return new CreateFunctionRequest(
                name,
                Collections.emptyList(),
                Collections.emptyList(),
                true,
                Map.of("sql", FunctionDefinition.sql("SELECT 1")),
                "test function",
                Collections.emptyMap()
        );
    }

    protected void createDatabaseAndTable() {
        catalog.createDatabase(prefix, new CreateDatabaseRequest(database, Collections.emptyMap()));
        catalog.createTable(prefix, database, new CreateTableRequest(Identifier.create(database, table), tableSchema()));
    }

    protected void triggerTableSnapshot(String database, String table) {
        try {
            var paimonCatalog = catalog.getUnderlyingCatalog();
            var paimonTable = paimonCatalog.getTable(Identifier.create(database, table));
            var writeBuilder = paimonTable.newBatchWriteBuilder();

            try (var tableWrite = writeBuilder.newWrite();
                 var tableCommit = writeBuilder.newCommit()) {
                tableWrite.write(GenericRow.of(1));
                tableCommit.commit(tableWrite.prepareCommit());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to trigger table snapshot for " + database + "." + table, e);
        }
    }

    protected void triggerTableSnapshotInBranch(String database, String table, String branch) {
        try {
            var paimonCatalog = catalog.getUnderlyingCatalog();
            var paimonTable = paimonCatalog.getTable(new Identifier(database, table, branch));
            var writeBuilder = paimonTable.newBatchWriteBuilder();

            try (var tableWrite = writeBuilder.newWrite();
                 var tableCommit = writeBuilder.newCommit()) {
                tableWrite.write(GenericRow.of(1));
                tableCommit.commit(tableWrite.prepareCommit());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to trigger table snapshot for " + database + "." + table, e);
        }
    }

    protected String uniqueName(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    protected Schema tableSchema() {
        return new Schema(
                List.of(new DataField(0, "id", DataTypes.INT())),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyMap(),
                "test"
        );
    }

    protected Schema partitionedTableSchema() {
        return new Schema(
                List.of(
                        new DataField(0, "id", DataTypes.INT()),
                        new DataField(1, "dt", DataTypes.STRING())
                ),
                List.of("dt"),
                Collections.emptyList(),
                Collections.emptyMap(),
                "partitioned-test"
        );
    }

    protected void triggerPartitionedTableSnapshot(String database, String table, int id, String dt) {
        try {
            var paimonCatalog = catalog.getUnderlyingCatalog();
            var paimonTable = paimonCatalog.getTable(Identifier.create(database, table));
            var writeBuilder = paimonTable.newBatchWriteBuilder();

            try (var tableWrite = writeBuilder.newWrite();
                 var tableCommit = writeBuilder.newCommit()) {
                tableWrite.write(GenericRow.of(id, BinaryString.fromString(dt)));
                tableCommit.commit(tableWrite.prepareCommit());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to trigger partitioned table snapshot for " + database + "." + table, e);
        }
    }

    protected ViewSchema viewSchema() {
        return new ViewSchema(
                List.of(new DataField(0, "id", DataTypes.INT())),
                "SELECT 1",
                Collections.emptyMap(),
                "test view",
                Collections.emptyMap()
        );
    }
}
