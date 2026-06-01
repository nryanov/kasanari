package kasanari.catalog.lance;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
import org.lance.namespace.model.ListNamespacesRequest;
import org.lance.namespace.model.ListTablesRequest;
import org.lance.namespace.model.NamespaceExistsRequest;
import org.lance.namespace.model.RegisterTableRequest;
import org.lance.namespace.model.RenameTableRequest;
import org.lance.namespace.model.TableExistsRequest;

import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        return false;
    }

    protected boolean supportsCreateTable() {
        return false;
    }

    protected boolean supportsCreateEmptyTable() {
        return true;
    }

    protected boolean supportsAlterTableAlterColumns() {
        return false;
    }

    protected boolean supportsAlterTableDropColumns() {
        return false;
    }

    @Test
    void createNamespace() {
        assumeTrue(supportsCreateNamespace() && supportsDescribeNamespace());
        createNamespaceEntity();
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
    void dropNamespace() {
        assumeTrue(supportsCreateNamespace() && supportsDropNamespace() && supportsListNamespaces());
        createNamespaceEntity();
        adapter.dropNamespace(new DropNamespaceRequest().id(namespaceId()).mode("fail"));
        var listed = adapter.listNamespaces(new ListNamespacesRequest().id(List.of()).limit(100));
        assertFalse(listed.getNamespaces().contains(namespaceName));
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
    void registerTable() {
        assumeTrue(supportsCreateNamespace() && supportsRegisterTable() && supportsDescribeTable());
        createNamespaceEntity();
        var expectedLocation = tableLocation();
        adapter.registerTable(new RegisterTableRequest().id(tableId()).location(expectedLocation).mode("create"));
        var described = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(expectedLocation, described.getLocation());
    }

    @Test
    void describeTable() {
        assumeTrue(supportsRegisterTable() && supportsDescribeTable());
        registerTableEntity();
        var response = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(tableName, response.getTable());
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
    void dropTable() {
        assumeTrue(supportsRegisterTable() && supportsDropTable() && supportsListTables());
        registerTableEntity();
        adapter.dropTable(new DropTableRequest().id(tableId()));
        var listed = adapter.listTables(new ListTablesRequest().id(namespaceId()).limit(100));
        assertFalse(listed.getTables().contains(tableName));
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
    void createTable() {
        assumeTrue(supportsCreateTable() && supportsDescribeTable());
        createTableEntity();
        var response = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(tableName, response.getTable());
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
        registerTableEntity();
        var alter = adapter.alterTableAlterColumns(new AlterTableAlterColumnsRequest().id(tableId()));
        var described = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(alter.getVersion(), described.getVersion());
    }

    @Test
    void alterTableDropColumns() {
        assumeTrue(supportsRegisterTable() && supportsAlterTableDropColumns() && supportsDescribeTable());
        registerTableEntity();
        var alter = adapter.alterTableDropColumns(new AlterTableDropColumnsRequest().id(tableId()).columns(List.of("col_a")));
        var described = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(alter.getVersion(), described.getVersion());
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
