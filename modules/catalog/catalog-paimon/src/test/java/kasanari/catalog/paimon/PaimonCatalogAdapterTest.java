package kasanari.catalog.paimon;

import org.apache.paimon.Snapshot;
import org.apache.paimon.catalog.Catalog;
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
import org.apache.paimon.schema.SchemaChange;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class PaimonCatalogAdapterTest {
    protected PaimonCatalogAdapter catalog;
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

    protected boolean supportsPaginatedMethods() {
        return false;
    }

    protected String registeredTablePath(String database, String table) {
        return "/tmp/" + database + "/" + table;
    }

    @Test
    void listDatabases() {
        assumeTrue(supportsDatabases());

        var request = new CreateDatabaseRequest(database, Collections.emptyMap());
        catalog.createDatabase(request);

        assertTrue(catalog.listDatabases(100, null).getDatabases().contains(database));
    }

    @Test
    void createDatabase() {
        assumeTrue(supportsDatabases());

        var request = new CreateDatabaseRequest(database, Map.of("owner", "test"));
        catalog.createDatabase(request);

        var created = catalog.getDatabase(database);

        assertEquals(database, created.getName());

        if (created.getOptions().containsKey("owner")) {
            assertEquals("test", created.getOptions().get("owner"));
        }
    }

    @Test
    void getDatabase() {
        assumeTrue(supportsDatabases());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        catalog.createDatabase(createDatabaseRequest);

        var expected = database;
        var result = catalog.getDatabase(database).getName();

        assertEquals(expected, result);
    }

    @Test
    void dropDatabase() {
        assumeTrue(supportsDatabases());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        catalog.createDatabase(createDatabaseRequest);
        catalog.dropDatabase(database);

        assertFalse(catalog.listDatabases(100, null).getDatabases().contains(database));
    }

    @Test
    void alterDatabase() {
        assumeTrue(supportsDatabases() && supportAlterDatabase());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var alterDatabaseRequest = new AlterDatabaseRequest(List.of("owner"), Map.of("owner", "updated"));
        catalog.createDatabase(createDatabaseRequest);

        var result = catalog.alterDatabase(database, alterDatabaseRequest);
        assertTrue(result.getUpdated().contains("owner"));
    }

    @Test
    void alterDatabaseWithNullCollections() {
        assumeTrue(supportsDatabases() && supportAlterDatabase());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var alterDatabaseRequest = new AlterDatabaseRequest(null, null);
        catalog.createDatabase(createDatabaseRequest);

        var expected = Collections.emptyList();
        var result = catalog.alterDatabase(database, alterDatabaseRequest).getUpdated();

        assertEquals(expected, result);
    }

    @Test
    void alterDatabaseFillsMissingProperties() {
        assumeTrue(supportsDatabases() && supportAlterDatabase());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Map.of("owner", "test"));
        var alterDatabaseRequest = new AlterDatabaseRequest(List.of("owner", "missing_property"), Map.of("retention", "7d"));
        catalog.createDatabase(createDatabaseRequest);

        var result = catalog.alterDatabase(database, alterDatabaseRequest);

        assertTrue(result.getRemoved().contains("owner"));
        assertTrue(result.getUpdated().contains("retention"));
        assertTrue(result.getMissing().contains("missing_property"));
    }

    @Test
    void registerTable() {
        assumeTrue(supportsDatabases() && supportsTables() && supportRegisterTable());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var registerTableRequest = new RegisterTableRequest(Identifier.create(database, table), registeredTablePath(database, table));
        catalog.createDatabase(createDatabaseRequest);
        catalog.registerTable(database, registerTableRequest);

        assertEquals(table, catalog.getTable(database, table).getName());
    }

    @Test
    void listTables() {
        assumeTrue(supportsDatabases() && supportsTables());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);

        assertTrue(catalog.listTables(database, 100, null, null).getTables().contains(table));
    }

    @Test
    void listTablesPaged() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsPaginatedMethods());

        var secondTable = uniqueName("table");
        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, new CreateTableRequest(Identifier.create(database, table), tableSchema()));
        catalog.createTable(database, new CreateTableRequest(Identifier.create(database, secondTable), tableSchema()));

        var firstPage = catalog.listTables(database, 1, null, null);
        var nextPageToken = firstPage.getNextPageToken();
        var secondPage = catalog.listTables(database, 1, nextPageToken, null);

        assertEquals(1, firstPage.getTables().size());
        assertNotNull(nextPageToken);
        assertEquals(1, secondPage.getTables().size());

        var names = new HashSet<String>();
        names.addAll(firstPage.getTables());
        names.addAll(secondPage.getTables());
        assertTrue(names.contains(table));
        assertTrue(names.contains(secondTable));
    }

    @Test
    void createTable() {
        assumeTrue(supportsDatabases() && supportsTables());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);

        assertEquals(table, catalog.getTable(database, table).getName());
    }

    @Test
    void listTableDetails() {
        assumeTrue(supportsDatabases() && supportsTables());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);

        var result = catalog.listTableDetails(database, 100, null, null, null)
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
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);

        var expected = List.of(Identifier.create(database, table));
        var result = catalog.listTablesGlobally(database, table, 100, null).getTables();

        assertEquals(expected, result);
    }

    @Test
    void listTablesGloballyPaged() {
        assumeTrue(supportsDatabases() && supportsTables() && supportListGlobally() && supportsPaginatedMethods());

        var secondTable = uniqueName("table");
        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, new CreateTableRequest(Identifier.create(database, table), tableSchema()));
        catalog.createTable(database, new CreateTableRequest(Identifier.create(database, secondTable), tableSchema()));

        var firstPage = catalog.listTablesGlobally(database, null, 1, null);
        var nextPageToken = firstPage.getNextPageToken();
        var secondPage = catalog.listTablesGlobally(database, null, 1, nextPageToken);

        assertEquals(1, firstPage.getTables().size());
        assertNotNull(nextPageToken);
        assertEquals(1, secondPage.getTables().size());

        var names = new HashSet<String>();
        names.addAll(firstPage.getTables().stream().map(Identifier::getTableName).toList());
        names.addAll(secondPage.getTables().stream().map(Identifier::getTableName).toList());
        assertTrue(names.contains(table));
        assertTrue(names.contains(secondTable));
    }

    @Test
    void getTable() {
        assumeTrue(supportsDatabases() && supportsTables());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);

        var expected = table;
        var result = catalog.getTable(database, table).getName();

        assertEquals(expected, result);
    }

    @Test
    void alterTable() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTableMutations() && supportAlterTable());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var alterTableRequest = new AlterTableRequest(Collections.emptyList());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        catalog.alterTable(database, table, alterTableRequest);

        assertEquals(table, catalog.getTable(database, table).getName());
    }

    @Test
    void alterTableWithNullChanges() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTableMutations() && supportAlterTable());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var alterTableRequest = new AlterTableRequest(null);
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        catalog.alterTable(database, table, alterTableRequest);

        assertEquals(table, catalog.getTable(database, table).getName());
    }

    @Test
    void dropTable() {
        assumeTrue(supportsDatabases() && supportsTables());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        catalog.dropTable(database, table);

        assertFalse(catalog.listTables(database, 100, null, null).getTables().contains(table));
    }

    @Test
    void renameTable() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTableMutations());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var renameTableRequest = new RenameTableRequest(Identifier.create(database, table), Identifier.create(database, renamedTable));
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        catalog.renameTable(renameTableRequest);

        var tables = catalog.listTables(database, 100, null, null).getTables();
        assertFalse(tables.contains(table));
        assertTrue(tables.contains(renamedTable));
    }

    @Test
    void commitTable() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTableMutations() && supportCommit());

        createDatabaseAndTable();

        // commitTable (commitSnapshot) is triggered internally via table.commit operation
        var latestSnapshotBeforeCommit = catalog.getVersionSnapshot(database, table, "LATEST").getSnapshot();
        triggerTableSnapshot(database, table);
        var latestSnapshotAfterCommit = catalog.getVersionSnapshot(database, table, "LATEST").getSnapshot();

        assertNull(latestSnapshotBeforeCommit);
        assertNotNull(latestSnapshotAfterCommit);
    }

    @Test
    void rollbackTable() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTableMutations() && supportRollbackTable());

        createDatabaseAndTable();
        triggerTableSnapshot(database, table);
        var snapshotId = catalog.getVersionSnapshot(database, table, "LATEST").getSnapshot().id();
        var rollbackTableRequest = new RollbackTableRequest(Instant.snapshot(snapshotId), null);
        catalog.rollbackTable(database, table, rollbackTableRequest);

        assertEquals(snapshotId, catalog.getVersionSnapshot(database, table, "LATEST").getSnapshot().id());
    }

    @Test
    void rollbackSchema() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTableMutations() && supportRollbackSchema());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var alterTableRequest = new AlterTableRequest(List.of(
                SchemaChange.addColumn("newField", DataTypes.INT())
        ));

        var rollbackSchemaRequest = new RollbackSchemaRequest(0L);
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        catalog.alterTable(database, table, alterTableRequest);
        catalog.rollbackSchema(database, table, rollbackSchemaRequest);

        assertEquals(table, catalog.getTable(database, table).getName());
    }

    @Test
    void rollbackSchemaShouldFailOnAttemptToRollbackOnDeletedSchema() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTableMutations() && supportRollbackSchema());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var alterTableRequest = new AlterTableRequest(List.of(
                SchemaChange.addColumn("newField", DataTypes.INT())
        ));

        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);

        // 2 schemas (0, 1) existing
        catalog.alterTable(database, table, alterTableRequest);

        // 1 schemas (0) existing
        assertDoesNotThrow(() -> catalog.rollbackSchema(database, table, new RollbackSchemaRequest(0L)));
        assertThrows(RuntimeException.class, () -> catalog.rollbackSchema(database, table, new RollbackSchemaRequest(1L)));
    }

    @Test
    void rollbackSchemaShouldFailOnAttemptToRollbackToPreviousSchemaIfNewOneAlreadyHasRelatedSnapshot() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTableMutations() && supportRollbackSchema());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var alterTableRequest = new AlterTableRequest(List.of(
                SchemaChange.addColumn("newField", DataTypes.INT())
        ));

        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);

        // 2 schemas (0, 1) existing
        catalog.alterTable(database, table, alterTableRequest);
        triggerTableSnapshotAltered(database, table);

        // failed attempt to roll back to previous schema
        assertThrows(RuntimeException.class, () -> catalog.rollbackSchema(database, table, new RollbackSchemaRequest(0L)));
    }

    @Test
    void authTableQuery() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTableMutations() && supportAuthTable());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var authTableQueryRequest = new AuthTableQueryRequest(null);
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);

        assertNotNull(catalog.authTableQuery(database, table, authTableQueryRequest));
    }

    @Test
    void getTableSnapshot() {
        assumeTrue(supportsDatabases() && supportsTables() && supportSnapshot());

        createDatabaseAndTable();
        triggerTableSnapshot(database, table);
        var latestSnapshot = catalog.getVersionSnapshot(database, table, "LATEST").getSnapshot();
        var result = catalog.getTableSnapshot(database, table).getSnapshot();

        assertNotNull(result);
        assertEquals(latestSnapshot.id(), result.snapshot().id());
    }

    @Test
    void getVersionSnapshot() {
        assumeTrue(supportsDatabases() && supportsTables() && supportSnapshot());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        triggerTableSnapshot(database, table);
        var latestSnapshot = catalog.getVersionSnapshot(database, table, "LATEST").getSnapshot();
        var result = catalog.getVersionSnapshot(database, table, "LATEST").getSnapshot();

        assertNotNull(result);
        assertEquals(latestSnapshot.id(), result.id());
    }

    @Test
    void listSnapshots() {
        assumeTrue(supportsDatabases() && supportsTables() && supportSnapshot());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        triggerTableSnapshot(database, table);

        var expected = List.of(catalog.getVersionSnapshot(database, table, "LATEST").getSnapshot().id());
        var result = catalog.listSnapshots(database, table, 100, null).getSnapshots();
        var resultIds = result.stream().map(Snapshot::id).toList();

        assertEquals(expected, resultIds);
    }

    @Test
    void listPartitions() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsPartitions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), partitionedTableSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        triggerPartitionedTableSnapshot(database, table, 1, "dt_a");
        triggerPartitionedTableSnapshot(database, table, 2, "dt_b");

        var result = catalog.listPartitions(database, table, 100, null, null).getPartitions();
        var specs = result.stream().map(PartitionStatistics::spec).toList();

        assertEquals(2, result.size());
        assertTrue(specs.contains(Map.of("dt", "dt_a")));
        assertTrue(specs.contains(Map.of("dt", "dt_b")));
    }

    @Test
    void listPartitionsPaged() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsPartitions() && supportsPaginatedMethods());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), partitionedTableSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        triggerPartitionedTableSnapshot(database, table, 1, "dt_a");
        triggerPartitionedTableSnapshot(database, table, 2, "dt_b");

        var firstPage = catalog.listPartitions(database, table, 1, null, null);
        var nextPageToken = firstPage.getNextPageToken();
        var secondPage = catalog.listPartitions(database, table, 1, nextPageToken, null);

        assertEquals(1, firstPage.getPartitions().size());
        assertNotNull(nextPageToken);
        assertEquals(1, secondPage.getPartitions().size());

        var specs = new HashSet<Map<String, String>>();
        specs.add(firstPage.getPartitions().get(0).spec());
        specs.add(secondPage.getPartitions().get(0).spec());
        assertTrue(specs.contains(Map.of("dt", "dt_a")));
        assertTrue(specs.contains(Map.of("dt", "dt_b")));
    }

    @Test
    void markDonePartitions() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsPartitions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), partitionedTableSchema());
        var markDonePartitionsRequest = new MarkDonePartitionsRequest(List.of(Map.of("dt", "dt_a")));
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        triggerPartitionedTableSnapshot(database, table, 1, "dt_a");
        catalog.markDonePartitions(database, table, markDonePartitionsRequest);
        var result = catalog.listPartitions(database, table, 100, null, null).getPartitions();
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
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        triggerPartitionedTableSnapshot(database, table, 1, "dt_a");
        triggerPartitionedTableSnapshot(database, table, 2, "dt_b");

        var result = catalog.listPartitionsByNames(database, table, listPartitionsByNamesRequest).getPartitions();
        var specs = result.stream().map(PartitionStatistics::spec).toList();

        assertEquals(1, result.size());
        assertEquals(List.of(Map.of("dt", "dt_a")), specs);
    }

    @Test
    void createPartitions() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsPartitions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), partitionedTableSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);

        try {
            catalog.getUnderlyingCatalog().createPartitions(
                    Identifier.create(database, table),
                    List.of(Map.of("dt", "dt_created"))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var partitions = catalog.listPartitions(database, table, 100, null, null).getPartitions();
        var specs = partitions.stream().map(PartitionStatistics::spec).toList();
        assertTrue(specs.contains(Map.of("dt", "dt_created")));
    }

    @Test
    void dropPartitions() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsPartitions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), partitionedTableSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);

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

        var partitions = catalog.listPartitions(database, table, 100, null, null).getPartitions();
        var specs = partitions.stream().map(PartitionStatistics::spec).toList();
        assertFalse(specs.contains(Map.of("dt", "dt_drop")));
    }

    @Test
    void alterPartitions() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsPartitions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), partitionedTableSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);

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

        var partitions = catalog.listPartitions(database, table, 100, null, null).getPartitions();
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
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);

        var expected = List.of("main");
        var result = catalog.listBranches(database, table).branches();

        assertEquals(expected, result);
    }

    @Test
    void createBranch() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsBranches());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var createBranchRequest = new CreateBranchRequest(branch, null);
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        catalog.createBranch(database, table, createBranchRequest);
        var branches = catalog.listBranches(database, table).branches();

        assertTrue(branches.contains(branch));
    }

    @Test
    void dropBranch() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsBranches());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var createBranchRequest = new CreateBranchRequest(branch, null);
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        catalog.createBranch(database, table, createBranchRequest);
        catalog.dropBranch(database, table, branch);
        var branches = catalog.listBranches(database, table).branches();

        assertFalse(branches.contains(branch));
    }

    @Test
    void renameBranch() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsBranches());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var createBranchRequest = new CreateBranchRequest(branch, null);
        var renameBranchRequest = new RenameBranchRequest(renamedBranch);
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        catalog.createBranch(database, table, createBranchRequest);
        catalog.renameBranch(database, table, branch, renameBranchRequest);
        var branches = catalog.listBranches(database, table).branches();

        assertFalse(branches.contains(branch));
        assertTrue(branches.contains(renamedBranch));
    }

    @Test
    void forwardBranch() throws Catalog.TableNotExistException, Catalog.TagAlreadyExistException {
        assumeTrue(supportsDatabases() && supportsTables() && supportsBranches());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var createBranchRequest = new CreateBranchRequest(branch, null);
        var forwardBranchRequest = new ForwardBranchRequest();

        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        triggerTableSnapshot(database, table);

        catalog.createBranch(database, table, createBranchRequest);
        triggerTableSnapshotInBranch(database, table, branch);
        var snapshot = catalog.getTableSnapshot(database, table);
        var snapshotId = snapshot.getSnapshot().snapshot().id();

        // paimon requires to have at least one tag on branch for fast-forward
        // https://github.com/apache/paimon/issues/7781
        catalog.getUnderlyingCatalog().createTag(new Identifier(database, table, branch), tag, snapshotId, null, true);

        catalog.forwardBranch(database, table, branch, forwardBranchRequest);

        var branches = catalog.listBranches(database, table).branches();

        assertTrue(branches.contains(branch));
    }

    @Test
    void listTags() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTags());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var createTagRequest = new CreateTagRequest(tag, null, null);
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        triggerTableSnapshot(database, table);
        catalog.createTag(database, table, createTagRequest);

        var expected = List.of(tag);
        var result = catalog.listTags(database, table, 100, null, null).tags();

        assertEquals(expected, result);
    }

    @Test
    void listTagsPaged() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTags() && supportsPaginatedMethods());

        var secondTag = uniqueName("tag");
        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        triggerTableSnapshot(database, table);
        catalog.createTag(database, table, new CreateTagRequest(tag, null, null));
        catalog.createTag(database, table, new CreateTagRequest(secondTag, null, null));

        var firstPage = catalog.listTags(database, table, 1, null, null);
        var nextPageToken = firstPage.getNextPageToken();
        var secondPage = catalog.listTags(database, table, 1, nextPageToken, null);

        assertEquals(1, firstPage.tags().size());
        assertNotNull(nextPageToken);
        assertEquals(1, secondPage.tags().size());

        var tags = new HashSet<String>();
        tags.addAll(firstPage.tags());
        tags.addAll(secondPage.tags());
        assertTrue(tags.contains(tag));
        assertTrue(tags.contains(secondTag));
    }

    @Test
    void createTag() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTags());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var createTagRequest = new CreateTagRequest(tag, null, null);
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        triggerTableSnapshot(database, table);
        catalog.createTag(database, table, createTagRequest);

        assertEquals(tag, catalog.getTag(database, table, tag).tagName());
    }

    @Test
    void getTag() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTags());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var createTagRequest = new CreateTagRequest(tag, null, null);
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        triggerTableSnapshot(database, table);
        catalog.createTag(database, table, createTagRequest);

        var expected = tag;
        var result = catalog.getTag(database, table, tag).tagName();

        assertEquals(expected, result);
    }

    @Test
    void deleteTag() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsTags());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        var createTagRequest = new CreateTagRequest(tag, null, null);
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);
        triggerTableSnapshot(database, table);
        catalog.createTag(database, table, createTagRequest);
        catalog.deleteTag(database, table, tag);

        assertEquals(Collections.emptyList(), catalog.listTags(database, table, 100, null, null).tags());
    }

    @Test
    void listEmptyConsumers() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsConsumers());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);

        var expected = Collections.emptyList();
        var result = catalog.listConsumers(database, table, 100, null).getConsumers();

        assertEquals(expected, result);
    }

    @Test
    void resetConsumer() {
        assumeTrue(supportsDatabases() && supportsTables() && supportsConsumers());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createTableRequest = new CreateTableRequest(Identifier.create(database, table), tableSchema());

        catalog.createDatabase(createDatabaseRequest);
        catalog.createTable(database, createTableRequest);

        triggerTableSnapshot(database, table);
        var snapshot = catalog.getTableSnapshot(database, table);
        var snapshotId = snapshot.getSnapshot().snapshot().id();
        var resetConsumerRequest = new ResetConsumerRequest(uniqueName("consumer"), snapshotId);

        catalog.resetConsumer(database, table, resetConsumerRequest);

        var consumers = catalog.listConsumers(database, table, 100, null).getConsumers();
        var consumerIds = consumers.stream().map(ConsumerInfo::getConsumerId).toList();

        assertEquals(List.of(resetConsumerRequest.consumerId()), consumerIds);
    }

    @Test
    void listViews() {
        assumeTrue(supportsDatabases() && supportsViews());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createViewRequest = new CreateViewRequest(Identifier.create(database, view), viewSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createView(database, createViewRequest);

        var expected = List.of(view);
        var result = catalog.listViews(database, 100, null, null).getViews().stream()
                .filter(expected::contains)
                .toList();

        assertEquals(expected, result);
    }

    @Test
    void listViewsPaged() {
        assumeTrue(supportsDatabases() && supportsViews() && supportsPaginatedMethods());

        var secondView = uniqueName("view");
        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createView(database, new CreateViewRequest(Identifier.create(database, view), viewSchema()));
        catalog.createView(database, new CreateViewRequest(Identifier.create(database, secondView), viewSchema()));

        var firstPage = catalog.listViews(database, 1, null, null);
        var nextPageToken = firstPage.getNextPageToken();
        var secondPage = catalog.listViews(database, 1, nextPageToken, null);

        assertEquals(1, firstPage.getViews().size());
        assertNotNull(nextPageToken);
        assertEquals(1, secondPage.getViews().size());

        var views = new HashSet<String>();
        views.addAll(firstPage.getViews());
        views.addAll(secondPage.getViews());
        assertTrue(views.contains(view));
        assertTrue(views.contains(secondView));
    }

    @Test
    void createView() {
        assumeTrue(supportsDatabases() && supportsViews());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createViewRequest = new CreateViewRequest(Identifier.create(database, view), viewSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createView(database, createViewRequest);

        assertEquals(view, catalog.getView(database, view).getName());
    }

    @Test
    void listViewDetails() {
        assumeTrue(supportsDatabases() && supportsViews());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createViewRequest = new CreateViewRequest(Identifier.create(database, view), viewSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createView(database, createViewRequest);

        var expected = List.of(view);
        var result = catalog.listViewDetails(database, 100, null, null)
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
        catalog.createDatabase(createDatabaseRequest);
        catalog.createView(database, createViewRequest);

        var expected = List.of(Identifier.create(database, view));
        var result = catalog.listViewsGlobally(database, view, 100, null).getViews();

        assertEquals(expected, result);
    }

    @Test
    void listViewsGloballyPaged() {
        assumeTrue(supportsDatabases() && supportsViews() && supportListGlobally() && supportsPaginatedMethods());

        var secondView = uniqueName("view");
        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createView(database, new CreateViewRequest(Identifier.create(database, view), viewSchema()));
        catalog.createView(database, new CreateViewRequest(Identifier.create(database, secondView), viewSchema()));

        var firstPage = catalog.listViewsGlobally(database, null, 1, null);
        var nextPageToken = firstPage.getNextPageToken();
        var secondPage = catalog.listViewsGlobally(database, null, 1, nextPageToken);

        assertEquals(1, firstPage.getViews().size());
        assertNotNull(nextPageToken);
        assertEquals(1, secondPage.getViews().size());

        var views = new HashSet<String>();
        views.addAll(firstPage.getViews().stream().map(Identifier::getTableName).toList());
        views.addAll(secondPage.getViews().stream().map(Identifier::getTableName).toList());
        assertTrue(views.contains(view));
        assertTrue(views.contains(secondView));
    }

    @Test
    void getView() {
        assumeTrue(supportsDatabases() && supportsViews());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createViewRequest = new CreateViewRequest(Identifier.create(database, view), viewSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createView(database, createViewRequest);

        var expected = view;
        var result = catalog.getView(database, view).getName();

        assertEquals(expected, result);
    }

    @Test
    void alterView() {
        assumeTrue(supportsDatabases() && supportsViews() && supportAlterView());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createViewRequest = new CreateViewRequest(Identifier.create(database, view), viewSchema());
        var alterViewRequest = new AlterViewRequest(Collections.emptyList());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createView(database, createViewRequest);
        catalog.alterView(database, view, alterViewRequest);

        assertEquals(view, catalog.getView(database, view).getName());
    }

    @Test
    void dropView() {
        assumeTrue(supportsDatabases() && supportsViews());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createViewRequest = new CreateViewRequest(Identifier.create(database, view), viewSchema());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createView(database, createViewRequest);
        catalog.dropView(database, view);

        assertEquals(Collections.emptyList(), catalog.listViews(database, 100, null, null).getViews());
    }

    @Test
    void renameView() {
        assumeTrue(supportsDatabases() && supportsViews());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createViewRequest = new CreateViewRequest(Identifier.create(database, view), viewSchema());
        var renameViewRequest = new RenameTableRequest(Identifier.create(database, view), Identifier.create(database, renamedView));
        catalog.createDatabase(createDatabaseRequest);
        catalog.createView(database, createViewRequest);
        catalog.renameView(renameViewRequest);
        var views = catalog.listViews(database, 100, null, null).getViews();

        assertFalse(views.contains(view));
        assertTrue(views.contains(renamedView));
    }

    @Test
    void listFunctions() {
        assumeTrue(supportsDatabases() && supportsFunctions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createFunctionRequest = functionRequest(function);
        catalog.createDatabase(createDatabaseRequest);
        catalog.createFunction(database, createFunctionRequest);

        var expected = List.of(function);
        var result = catalog.listFunctions(database, 100, null, null).functions().stream()
                .filter(expected::contains)
                .toList();

        assertEquals(expected, result);
    }

    @Test
    void listFunctionsPaged() {
        assumeTrue(supportsDatabases() && supportsFunctions() && supportsPaginatedMethods());

        var secondFunction = uniqueName("fn");
        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createFunction(database, functionRequest(function));
        catalog.createFunction(database, functionRequest(secondFunction));

        var firstPage = catalog.listFunctions(database, 1, null, null);
        var nextPageToken = firstPage.getNextPageToken();
        var secondPage = catalog.listFunctions(database, 1, nextPageToken, null);

        assertEquals(1, firstPage.functions().size());
        assertNotNull(nextPageToken);
        assertEquals(1, secondPage.functions().size());

        var functions = new HashSet<String>();
        functions.addAll(firstPage.functions());
        functions.addAll(secondPage.functions());
        assertTrue(functions.contains(function));
        assertTrue(functions.contains(secondFunction));
    }

    @Test
    void createFunction() {
        assumeTrue(supportsDatabases() && supportsFunctions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createFunctionRequest = functionRequest(function);
        catalog.createDatabase(createDatabaseRequest);
        catalog.createFunction(database, createFunctionRequest);

        assertEquals(function, catalog.getFunction(database, function).name());
    }

    @Test
    void listFunctionDetails() {
        assumeTrue(supportsDatabases() && supportsFunctions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createFunctionRequest = functionRequest(function);

        catalog.createDatabase(createDatabaseRequest);
        catalog.createFunction(database, createFunctionRequest);

        var expected = List.of(function);
        var result = catalog.listFunctionDetails(database, 100, null, null)
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
        catalog.createDatabase(createDatabaseRequest);
        catalog.createFunction(database, createFunctionRequest);

        var expected = List.of(Identifier.create(database, function));
        var result = catalog.listFunctionsGlobally(database, function, 100, null).functions();

        assertEquals(expected, result);
    }

    @Test
    void listFunctionsGloballyPaged() {
        assumeTrue(supportsDatabases() && supportsFunctions() && supportListGlobally() && supportsPaginatedMethods());

        var secondFunction = uniqueName("fn");
        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createFunction(database, functionRequest(function));
        catalog.createFunction(database, functionRequest(secondFunction));

        var firstPage = catalog.listFunctionsGlobally(database, null, 1, null);
        var nextPageToken = firstPage.getNextPageToken();
        var secondPage = catalog.listFunctionsGlobally(database, null, 1, nextPageToken);

        assertEquals(1, firstPage.functions().size());
        assertNotNull(nextPageToken);
        assertEquals(1, secondPage.functions().size());

        var functions = new HashSet<String>();
        functions.addAll(firstPage.functions().stream().map(Identifier::getTableName).toList());
        functions.addAll(secondPage.functions().stream().map(Identifier::getTableName).toList());
        assertTrue(functions.contains(function));
        assertTrue(functions.contains(secondFunction));
    }

    @Test
    void getFunction() {
        assumeTrue(supportsDatabases() && supportsFunctions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createFunctionRequest = functionRequest(function);
        catalog.createDatabase(createDatabaseRequest);
        catalog.createFunction(database, createFunctionRequest);

        var expected = function;
        var result = catalog.getFunction(database, function).name();

        assertEquals(expected, result);
    }

    @Test
    void alterFunction() {
        assumeTrue(supportsDatabases() && supportsFunctions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createFunctionRequest = functionRequest(function);
        var alterFunctionRequest = new AlterFunctionRequest(Collections.emptyList());
        catalog.createDatabase(createDatabaseRequest);
        catalog.createFunction(database, createFunctionRequest);
        catalog.alterFunction(database, function, alterFunctionRequest);

        assertEquals(function, catalog.getFunction(database, function).name());
    }

    @Test
    void dropFunction() {
        assumeTrue(supportsDatabases() && supportsFunctions());

        var createDatabaseRequest = new CreateDatabaseRequest(database, Collections.emptyMap());
        var createFunctionRequest = functionRequest(function);
        catalog.createDatabase(createDatabaseRequest);
        catalog.createFunction(database, createFunctionRequest);
        catalog.dropFunction(database, function);

        assertEquals(Collections.emptyList(), catalog.listFunctions(database, 100, null, null).functions());
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
        catalog.createDatabase(new CreateDatabaseRequest(database, Collections.emptyMap()));
        catalog.createTable(database, new CreateTableRequest(Identifier.create(database, table), tableSchema()));
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

    protected void triggerTableSnapshotAltered(String database, String table) {
        try {
            var paimonCatalog = catalog.getUnderlyingCatalog();
            var paimonTable = paimonCatalog.getTable(Identifier.create(database, table));
            var writeBuilder = paimonTable.newBatchWriteBuilder();

            try (var tableWrite = writeBuilder.newWrite();
                 var tableCommit = writeBuilder.newCommit()) {
                tableWrite.write(GenericRow.of(1, 2));
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
