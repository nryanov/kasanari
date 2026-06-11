package kasanari.catalog.lance;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.apache.arrow.memory.RootAllocator;
import org.lance.Dataset;
import org.lance.namespace.errors.NamespaceAlreadyExistsException;
import org.lance.namespace.errors.NamespaceNotEmptyException;
import org.lance.namespace.errors.NamespaceNotFoundException;
import org.lance.namespace.errors.TableAlreadyExistsException;
import org.lance.namespace.errors.TableNotFoundException;
import org.lance.namespace.model.AlterColumnsEntry;
import org.lance.namespace.model.AlterTableAlterColumnsRequest;
import org.lance.namespace.model.AlterTableDropColumnsRequest;
import org.lance.namespace.model.CreateNamespaceRequest;
import org.lance.namespace.model.CreateTableRequest;
import org.lance.namespace.model.DeclareTableRequest;
import org.lance.namespace.model.DeregisterTableRequest;
import org.lance.namespace.model.DescribeNamespaceRequest;
import org.lance.namespace.model.DescribeTableRequest;
import org.lance.namespace.model.DropNamespaceRequest;
import org.lance.namespace.model.DropTableRequest;
import org.lance.namespace.model.JsonArrowField;
import org.lance.namespace.model.ListNamespacesRequest;
import org.lance.namespace.model.ListTablesRequest;
import org.lance.namespace.model.NamespaceExistsRequest;
import org.lance.namespace.model.RegisterTableRequest;
import org.lance.namespace.model.RenameTableRequest;
import org.lance.namespace.model.TableExistsRequest;

import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.nio.file.Files;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class LanceCatalogAdapterTest {
    protected LanceCatalogAdapter adapter;

    protected String namespaceName;
    protected String tableName;
    protected String indexName;
    protected String tagName;
    protected String transactionName;

    @BeforeAll
    final void setup() throws Exception {
        adapter = setupCatalogAdapter();
    }

    protected abstract LanceCatalogAdapter setupCatalogAdapter() throws Exception;

    @BeforeEach
    final void beforeEach() {
        namespaceName = uniqueName("ns");
        tableName = uniqueName("table");
        indexName = uniqueName("index");
        tagName = uniqueName("tag");
        transactionName = uniqueName("txn");
        reset();
    }

    @AfterAll
    final void afterAll() throws Exception {
        if (adapter != null) {
            adapter.close();
        }
        onClose();
    }

    protected void reset() {
    }

    protected void onClose() {
    }

    protected String uniqueName(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    protected List<String> namespaceId() {
        return List.of(namespaceName);
    }

    protected List<String> tableId() {
        return List.of(namespaceName, tableName);
    }

    protected void createNamespaceEntity() {
        var request = new CreateNamespaceRequest().id(namespaceId()).mode("create").properties(Map.of("owner", "ci"));
        adapter.createNamespace(request);
    }

    protected void registerTableEntity() {
        createNamespaceEntity();
        var location = tableLocation();
        adapter.registerTable(new RegisterTableRequest().id(tableId()).location(location).mode("create"));
    }

    protected void registerMaterializedTableEntity() {
        createNamespaceEntity();
        var location = localTableLocation();
        try (var allocator = new RootAllocator();
             var ignored = Dataset.write()
                     .allocator(allocator)
                     .uri(location)
                     .schema(LanceArrowIpc.TABLE_SCHEMA)
                     .execute()) {
            adapter.registerTable(new RegisterTableRequest().id(tableId()).location(location).mode("create"));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create materialized Lance dataset for tests", e);
        }
    }

    protected String localTableLocation() {
        try {
            return Files.createTempDirectory("kasanari-lance-" + tableName).toUri().toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create local table location for tests", e);
        }
    }

    protected void createTableEntity() {
        createNamespaceEntity();
        adapter.createTable(
                new CreateTableRequest().id(tableId()).properties(Map.of("stage", "created")),
                LanceArrowIpc.emptyBatch());
    }

    protected void createEmptyTableEntity() {
        createNamespaceEntity();
        adapter.createEmptyTable(new DeclareTableRequest().id(tableId()).location(tableLocation()));
    }

    protected String tableLocation() {
        return "s3://warehouse/" + namespaceName + "/" + tableName;
    }

    protected boolean supportsCreateNamespace() {
        return true;
    }

    protected boolean supportsDescribeNamespace() {
        return true;
    }

    protected boolean supportsDropNamespace() {
        return true;
    }

    protected boolean supportsNamespaceExists() {
        return true;
    }

    protected boolean supportsListNamespaces() {
        return true;
    }

    protected boolean supportsListTables() {
        return true;
    }

    protected boolean supportsRegisterTable() {
        return true;
    }

    protected boolean supportsDescribeTable() {
        return true;
    }

    protected boolean supportsTableExists() {
        return true;
    }

    protected boolean supportsDropTable() {
        return true;
    }

    protected boolean supportsDeregisterTable() {
        return true;
    }

    protected boolean supportsRenameTable() {
        return true;
    }

    protected boolean supportsCreateTable() {
        return true;
    }

    protected boolean supportsCreateEmptyTable() {
        return true;
    }

    protected boolean supportsAlterTableAlterColumns() {
        return true;
    }

    protected boolean supportsAlterTableDropColumns() {
        return true;
    }

    protected boolean supportsNamespaceModeVariants() {
        return true;
    }

    protected boolean supportsDropNamespaceBehaviorVariants() {
        return true;
    }

    protected boolean supportsRegisterTableModeVariants() {
        return true;
    }

    protected boolean supportsCreateTableModeVariants() {
        return true;
    }

    protected boolean supportsInvalidPageTokenFailure() {
        return true;
    }

    protected boolean supportsMissingTableExistsError() {
        return true;
    }

    @Test
    void createNamespace() {
        assumeTrue(supportsCreateNamespace() && supportsDescribeNamespace());
        createNamespaceEntity();
        var response = adapter.describeNamespace(new DescribeNamespaceRequest().id(namespaceId()));
        assertEquals("ci", response.getProperties().get("owner"));
    }

    @Test
    void createNamespaceDuplicateCreateModeThrows() {
        assumeTrue(supportsCreateNamespace() && supportsNamespaceModeVariants());
        createNamespaceEntity();
        assertThrows(
                NamespaceAlreadyExistsException.class,
                () -> adapter.createNamespace(new CreateNamespaceRequest().id(namespaceId()).mode("create"))
        );
    }

    @Test
    void createNamespaceExistOkIsIdempotent() {
        assumeTrue(supportsCreateNamespace() && supportsDescribeNamespace() && supportsNamespaceModeVariants());
        createNamespaceEntity();

        assertDoesNotThrow(() -> adapter.createNamespace(
                new CreateNamespaceRequest()
                        .id(namespaceId())
                        .mode("exist_ok")
                        .properties(Map.of("owner", "ci"))
        ));

        var response = adapter.describeNamespace(new DescribeNamespaceRequest().id(namespaceId()));
        assertEquals("ci", response.getProperties().get("owner"));
    }

    @Test
    void describeNamespace() {
        assumeTrue(supportsCreateNamespace() && supportsDescribeNamespace());
        createNamespaceEntity();
        var response = adapter.describeNamespace(new DescribeNamespaceRequest().id(namespaceId()));
        assertEquals("ci", response.getProperties().get("owner"));
    }

    @Test
    void describeNamespaceMissingThrows() {
        assumeTrue(supportsDescribeNamespace());
        assertThrows(
                NamespaceNotFoundException.class,
                () -> adapter.describeNamespace(new DescribeNamespaceRequest().id(namespaceId()))
        );
    }

    @Test
    void dropNamespace() {
        assumeTrue(supportsCreateNamespace() && supportsDropNamespace() && supportsListNamespaces());
        createNamespaceEntity();
        adapter.dropNamespace(new DropNamespaceRequest().id(namespaceId()).mode("fail"));
        var listed = adapter.listNamespaces(new ListNamespacesRequest().id(List.of()).limit(100));
        assertFalse(listed.getNamespaces().contains(namespaceName));
    }

    @Test
    void dropNamespaceMissingSkipMode() {
        assumeTrue(supportsDropNamespace() && supportsNamespaceModeVariants());
        assertDoesNotThrow(() -> adapter.dropNamespace(new DropNamespaceRequest().id(namespaceId()).mode("skip")));
    }

    @Test
    void dropNamespaceMissingFailModeThrows() {
        assumeTrue(supportsDropNamespace() && supportsNamespaceModeVariants());
        assertThrows(
                NamespaceNotFoundException.class,
                () -> adapter.dropNamespace(new DropNamespaceRequest().id(namespaceId()).mode("fail"))
        );
    }

    @Test
    void dropNamespaceRestrictOnNonEmptyThrows() {
        assumeTrue(
                supportsCreateNamespace()
                        && supportsRegisterTable()
                        && supportsDropNamespace()
                        && supportsDropNamespaceBehaviorVariants()
        );
        registerTableEntity();
        assertThrows(
                NamespaceNotEmptyException.class,
                () -> adapter.dropNamespace(new DropNamespaceRequest().id(namespaceId()).behavior("restrict"))
        );
    }

    @Test
    void dropNamespaceCascadeOnNonEmptyRemovesNamespaceAndTables() {
        assumeTrue(
                supportsCreateNamespace()
                        && supportsRegisterTable()
                        && supportsDropNamespace()
                        && supportsListNamespaces()
                        && supportsListTables()
                        && supportsDropNamespaceBehaviorVariants()
        );
        registerTableEntity();

        adapter.dropNamespace(new DropNamespaceRequest().id(namespaceId()).behavior("cascade"));

        var namespaces = adapter.listNamespaces(new ListNamespacesRequest().id(List.of()).limit(100));
        assertFalse(namespaces.getNamespaces().contains(namespaceName));

        var tables = adapter.listTables(new ListTablesRequest().id(namespaceId()).limit(100));
        assertFalse(tables.getTables().contains(tableName));
    }

    @Test
    void namespaceExists() {
        assumeTrue(supportsCreateNamespace() && supportsNamespaceExists());
        createNamespaceEntity();
        var result = assertDoesNotThrow(() -> {
            adapter.namespaceExists(new NamespaceExistsRequest().id(namespaceId()));
            return true;
        });
        assertEquals(true, result);
    }

    @Test
    void listNamespaces() {
        assumeTrue(supportsCreateNamespace() && supportsListNamespaces());
        createNamespaceEntity();
        var response = adapter.listNamespaces(new ListNamespacesRequest().id(List.of()).limit(100));
        assertTrue(response.getNamespaces().contains(namespaceName));
    }

    @Test
    void listTables() {
        assumeTrue(supportsRegisterTable() && supportsListTables());
        registerTableEntity();
        var response = adapter.listTables(new ListTablesRequest().id(namespaceId()).limit(100));
        assertTrue(response.getTables().contains(tableName));
    }

    @Test
    void listNamespacesPaginated() {
        assumeTrue(supportsCreateNamespace() && supportsListNamespaces());
        var firstNamespace = uniqueName("ns_page");
        var secondNamespace = uniqueName("ns_page");

        adapter.createNamespace(new CreateNamespaceRequest()
                .id(List.of(firstNamespace))
                .mode("create")
                .properties(Map.of("owner", "ci")));
        adapter.createNamespace(new CreateNamespaceRequest()
                .id(List.of(secondNamespace))
                .mode("create")
                .properties(Map.of("owner", "ci")));

        var firstPage = adapter.listNamespaces(new ListNamespacesRequest().id(List.of()).limit(1));
        assertEquals(1, firstPage.getNamespaces().size());
        assertTrue(firstPage.getPageToken() != null && !firstPage.getPageToken().isBlank());

        var secondPage = adapter.listNamespaces(new ListNamespacesRequest()
                .id(List.of())
                .limit(1)
                .pageToken(firstPage.getPageToken()));
        assertEquals(1, secondPage.getNamespaces().size());
        assertNull(secondPage.getPageToken());

        var actual = new HashSet<String>();
        actual.addAll(firstPage.getNamespaces());
        actual.addAll(secondPage.getNamespaces());

        assertEquals(new HashSet<>(List.of(firstNamespace, secondNamespace)), actual);
    }

    @Test
    void listNamespacesInvalidPageTokenThrows() {
        assumeTrue(supportsListNamespaces() && supportsInvalidPageTokenFailure());
        assertThrows(
                IllegalArgumentException.class,
                () -> adapter.listNamespaces(new ListNamespacesRequest().id(List.of()).limit(1).pageToken("bad-token"))
        );
    }

    @Test
    void listTablesPaginated() {
        assumeTrue(supportsCreateNamespace() && supportsRegisterTable() && supportsListTables());
        var namespace = uniqueName("ns_page");
        var firstTable = uniqueName("table_page");
        var secondTable = uniqueName("table_page");

        adapter.createNamespace(new CreateNamespaceRequest()
                .id(List.of(namespace))
                .mode("create")
                .properties(Map.of("owner", "ci")));
        adapter.registerTable(new RegisterTableRequest()
                .id(List.of(namespace, firstTable))
                .location("s3://warehouse/" + namespace + "/" + firstTable)
                .mode("create"));
        adapter.registerTable(new RegisterTableRequest()
                .id(List.of(namespace, secondTable))
                .location("s3://warehouse/" + namespace + "/" + secondTable)
                .mode("create"));

        var firstPage = adapter.listTables(new ListTablesRequest().id(List.of(namespace)).limit(1));
        assertEquals(1, firstPage.getTables().size());
        assertTrue(firstPage.getPageToken() != null && !firstPage.getPageToken().isBlank());

        var secondPage = adapter.listTables(new ListTablesRequest()
                .id(List.of(namespace))
                .limit(1)
                .pageToken(firstPage.getPageToken()));
        assertEquals(1, secondPage.getTables().size());
        assertNull(secondPage.getPageToken());

        var actual = new HashSet<String>();
        actual.addAll(firstPage.getTables());
        actual.addAll(secondPage.getTables());

        assertEquals(new HashSet<>(List.of(firstTable, secondTable)), actual);
    }

    @Test
    void listTablesInvalidPageTokenThrows() {
        assumeTrue(supportsListTables() && supportsInvalidPageTokenFailure());
        assertThrows(
                IllegalArgumentException.class,
                () -> adapter.listTables(new ListTablesRequest().id(namespaceId()).limit(1).pageToken("bad-token"))
        );
    }

    @Test
    void registerTable() {
        assumeTrue(supportsCreateNamespace() && supportsRegisterTable() && supportsDescribeTable());
        createNamespaceEntity();
        var expectedLocation = tableLocation();
        adapter.registerTable(new RegisterTableRequest().id(tableId()).location(expectedLocation).mode("create"));
        var described = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(expectedLocation, described.getLocation());
    }

    @Test
    void registerTableDuplicateCreateModeThrows() {
        assumeTrue(supportsRegisterTable() && supportsRegisterTableModeVariants());
        registerTableEntity();
        assertThrows(
                TableAlreadyExistsException.class,
                () -> adapter.registerTable(new RegisterTableRequest().id(tableId()).location(tableLocation()).mode("create"))
        );
    }

    @Test
    void registerTableOverwriteModeUpdatesLocation() {
        assumeTrue(supportsRegisterTable() && supportsDescribeTable() && supportsRegisterTableModeVariants());
        registerTableEntity();

        var updatedLocation = tableLocation() + "_overwritten";
        adapter.registerTable(new RegisterTableRequest().id(tableId()).location(updatedLocation).mode("overwrite"));

        var described = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(updatedLocation, described.getLocation());
    }

    @Test
    void describeTable() {
        assumeTrue(supportsRegisterTable() && supportsDescribeTable());
        registerTableEntity();
        var response = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(tableName, response.getTable());
    }

    @Test
    void describeTableReturnsSchemaWhenDatasetExists() {
        assumeTrue(supportsRegisterTable() && supportsDescribeTable());
        registerMaterializedTableEntity();
        var response = adapter.describeTable(new DescribeTableRequest().id(tableId()));

        assertNotNull(response.getSchema());
        assertEquals(2, response.getSchema().getFields().size());
        assertEquals("id", response.getSchema().getFields().get(0).getName());
        assertEquals("int64", response.getSchema().getFields().get(0).getType().getType());
        assertEquals("col_a", response.getSchema().getFields().get(1).getName());
        assertEquals("utf8", response.getSchema().getFields().get(1).getType().getType());
        assertNotNull(response.getVersion());
    }

    @Test
    void describeTableReturnsBaseResponseWhenDatasetMissing() {
        assumeTrue(supportsRegisterTable() && supportsDescribeTable());
        registerTableEntity();
        var response = adapter.describeTable(new DescribeTableRequest().id(tableId()));

        assertEquals(tableName, response.getTable());
        assertEquals(tableLocation(), response.getLocation());
        assertNull(response.getSchema());
        assertNull(response.getVersion());
    }

    @Test
    void describeTableMissingThrows() {
        assumeTrue(supportsDescribeTable());
        assertThrows(
                TableNotFoundException.class,
                () -> adapter.describeTable(new DescribeTableRequest().id(tableId()))
        );
    }

    @Test
    void tableExists() {
        assumeTrue(supportsRegisterTable() && supportsTableExists());
        registerTableEntity();
        var result = assertDoesNotThrow(() -> {
            adapter.tableExists(new TableExistsRequest().id(tableId()));
            return true;
        });
        assertEquals(true, result);
    }

    @Test
    void tableExistsMissingThrows() {
        assumeTrue(supportsTableExists() && supportsMissingTableExistsError());
        assertThrows(
                IllegalStateException.class,
                () -> adapter.tableExists(new TableExistsRequest().id(tableId()))
        );
    }

    @Test
    void dropTable() {
        assumeTrue(supportsRegisterTable() && supportsDropTable() && supportsListTables());
        registerTableEntity();
        adapter.dropTable(new DropTableRequest().id(tableId()));
        var listed = adapter.listTables(new ListTablesRequest().id(namespaceId()).limit(100));
        assertFalse(listed.getTables().contains(tableName));
    }

    @Test
    void dropTableMissingThrows() {
        assumeTrue(supportsDropTable());
        assertThrows(
                TableNotFoundException.class,
                () -> adapter.dropTable(new DropTableRequest().id(tableId()))
        );
    }

    @Test
    void deregisterTable() {
        assumeTrue(supportsRegisterTable() && supportsDeregisterTable() && supportsListTables());
        registerTableEntity();
        adapter.deregisterTable(new DeregisterTableRequest().id(tableId()));
        var listed = adapter.listTables(new ListTablesRequest().id(namespaceId()).limit(100));
        assertFalse(listed.getTables().contains(tableName));
    }

    @Test
    void deregisterTableMissingThrows() {
        assumeTrue(supportsDeregisterTable());
        assertThrows(
                TableNotFoundException.class,
                () -> adapter.deregisterTable(new DeregisterTableRequest().id(tableId()))
        );
    }

    @Test
    void createTable() {
        assumeTrue(supportsCreateTable() && supportsDescribeTable());
        createTableEntity();
        var response = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(tableName, response.getTable());
    }

    @Test
    void createTableDuplicateCreateModeThrows() {
        assumeTrue(supportsCreateTable() && supportsCreateTableModeVariants());
        createTableEntity();
        assertThrows(
                TableAlreadyExistsException.class,
                () -> adapter.createTable(new CreateTableRequest().id(tableId()).mode("create"), LanceArrowIpc.emptyBatch())
        );
    }

    @Test
    void createTableExistOkIsIdempotent() {
        assumeTrue(supportsCreateTable() && supportsDescribeTable() && supportsCreateTableModeVariants());
        createTableEntity();

        assertDoesNotThrow(() ->
                adapter.createTable(new CreateTableRequest().id(tableId()).mode("existok"), LanceArrowIpc.emptyBatch())
        );

        var described = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(tableName, described.getTable());
        assertNotEquals("", described.getLocation());
    }

    @Test
    void createEmptyTable() {
        assumeTrue(supportsCreateEmptyTable() && supportsDescribeTable());
        createEmptyTableEntity();
        var response = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(tableName, response.getTable());
    }

    @Test
    void alterTableAlterColumns() {
        assumeTrue(supportsRegisterTable() && supportsAlterTableAlterColumns() && supportsDescribeTable());
        createTableEntity();

        var alterColumnEntry = new AlterColumnsEntry()
                .nullable(true)
                .path("col_a")
                .rename("renamed_column_a");

        var alter = adapter.alterTableAlterColumns(new AlterTableAlterColumnsRequest().addAlterationsItem(alterColumnEntry).id(tableId()));
        var described = adapter.describeTable(new DescribeTableRequest().id(tableId()));

        assertEquals(alter.getVersion(), described.getVersion());
        assertNotNull(described.getSchema());
        assertNotNull(described.getSchema().getFields());

        var expectedFieldNames = Set.of("id", "renamed_column_a");
        var actualFieldNames = described.getSchema().getFields().stream().map(JsonArrowField::getName).collect(Collectors.toSet());
        assertEquals(expectedFieldNames, actualFieldNames);
    }

    @Test
    void alterTableDropColumns() {
        assumeTrue(supportsRegisterTable() && supportsAlterTableDropColumns() && supportsDescribeTable());
        createTableEntity();
        var alter = adapter.alterTableDropColumns(new AlterTableDropColumnsRequest().id(tableId()).columns(List.of("col_a")));
        var described = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(alter.getVersion(), described.getVersion());

        assertNotNull(described.getSchema());
        assertNotNull(described.getSchema().getFields());

        var expectedFieldNames = Set.of("id");
        var actualFieldNames = described.getSchema().getFields().stream().map(JsonArrowField::getName).collect(Collectors.toSet());
        assertEquals(expectedFieldNames, actualFieldNames);
    }

    @Test
    void renameTable() {
        assumeTrue(supportsRegisterTable() && supportsRenameTable() && supportsDescribeTable());
        registerTableEntity();
        var renamed = uniqueName("renamed");
        adapter.renameTable(new RenameTableRequest().id(tableId()).newTableName(renamed));
        var described = adapter.describeTable(new DescribeTableRequest().id(List.of(namespaceName, renamed)));
        assertEquals(renamed, described.getTable());
    }
}
