package kasanari.catalog.lance;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.lance.namespace.model.AlterTableAddColumnsRequest;
import org.lance.namespace.model.AlterTableAlterColumnsRequest;
import org.lance.namespace.model.AlterTableDropColumnsRequest;
import org.lance.namespace.model.AlterTransactionRequest;
import org.lance.namespace.model.AnalyzeTableQueryPlanRequest;
import org.lance.namespace.model.BatchCommitTablesRequest;
import org.lance.namespace.model.BatchCreateTableVersionsRequest;
import org.lance.namespace.model.BatchDeleteTableVersionsRequest;
import org.lance.namespace.model.CountTableRowsRequest;
import org.lance.namespace.model.CreateNamespaceRequest;
import org.lance.namespace.model.CreateTableIndexRequest;
import org.lance.namespace.model.CreateTableRequest;
import org.lance.namespace.model.CreateTableTagRequest;
import org.lance.namespace.model.CreateTableVersionRequest;
import org.lance.namespace.model.DeleteFromTableRequest;
import org.lance.namespace.model.DeleteTableTagRequest;
import org.lance.namespace.model.DeregisterTableRequest;
import org.lance.namespace.model.DescribeNamespaceRequest;
import org.lance.namespace.model.DescribeTableIndexStatsRequest;
import org.lance.namespace.model.DescribeTableRequest;
import org.lance.namespace.model.DescribeTableVersionRequest;
import org.lance.namespace.model.DescribeTransactionRequest;
import org.lance.namespace.model.DropNamespaceRequest;
import org.lance.namespace.model.DropTableIndexRequest;
import org.lance.namespace.model.DropTableRequest;
import org.lance.namespace.model.ExplainTableQueryPlanRequest;
import org.lance.namespace.model.GetTableStatsRequest;
import org.lance.namespace.model.GetTableTagVersionRequest;
import org.lance.namespace.model.InsertIntoTableRequest;
import org.lance.namespace.model.ListNamespacesRequest;
import org.lance.namespace.model.ListTableIndicesRequest;
import org.lance.namespace.model.ListTableTagsRequest;
import org.lance.namespace.model.ListTableVersionsRequest;
import org.lance.namespace.model.ListTablesRequest;
import org.lance.namespace.model.MergeInsertIntoTableRequest;
import org.lance.namespace.model.NamespaceExistsRequest;
import org.lance.namespace.model.QueryTableRequest;
import org.lance.namespace.model.RegisterTableRequest;
import org.lance.namespace.model.RestoreTableRequest;
import org.lance.namespace.model.TableExistsRequest;
import org.lance.namespace.model.UpdateTableRequest;
import org.lance.namespace.model.UpdateTableTagRequest;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    protected byte[] requestData() {
        return uniqueName("payload").getBytes(StandardCharsets.UTF_8);
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

    protected void createTableEntity() {
        createNamespaceEntity();
        var request = new CreateTableRequest().id(tableId()).mode("create").properties(Map.of("stage", "created"));
        adapter.createTable(request, requestData());
    }

    protected boolean supportsCreateNamespace() {
        return false;
    }

    protected boolean supportsDescribeNamespace() {
        return false;
    }

    protected boolean supportsDropNamespace() {
        return false;
    }

    protected boolean supportsNamespaceExists() {
        return false;
    }

    protected boolean supportsListNamespaces() {
        return false;
    }

    protected boolean supportsListTables() {
        return false;
    }

    protected boolean supportsRegisterTable() {
        return false;
    }

    protected boolean supportsDescribeTable() {
        return false;
    }

    protected boolean supportsTableExists() {
        return false;
    }

    protected boolean supportsDropTable() {
        return false;
    }

    protected boolean supportsDeregisterTable() {
        return false;
    }

    protected boolean supportsAlterTableAddColumns() {
        return false;
    }

    protected boolean supportsAlterTableAlterColumns() {
        return false;
    }

    protected boolean supportsAlterTableDropColumns() {
        return false;
    }

    protected boolean supportsAnalyzeTableQueryPlan() {
        return false;
    }

    protected boolean supportsCountTableRows() {
        return false;
    }

    protected boolean supportsCreateTable() {
        return false;
    }

    protected boolean supportsCreateTableIndex() {
        return false;
    }

    protected boolean supportsCreateTableTag() {
        return false;
    }

    protected boolean supportsDeleteFromTable() {
        return false;
    }

    protected boolean supportsDeleteTableTag() {
        return false;
    }

    protected boolean supportsDescribeTableIndexStats() {
        return false;
    }

    protected boolean supportsDropTableIndex() {
        return false;
    }

    protected boolean supportsExplainTableQueryPlan() {
        return false;
    }

    protected boolean supportsGetTableStats() {
        return false;
    }

    protected boolean supportsGetTableTagVersion() {
        return false;
    }

    protected boolean supportsInsertIntoTable() {
        return false;
    }

    protected boolean supportsListTableIndices() {
        return false;
    }

    protected boolean supportsListTableTags() {
        return false;
    }

    protected boolean supportsListTableVersions() {
        return false;
    }

    protected boolean supportsCreateTableVersion() {
        return false;
    }

    protected boolean supportsDescribeTableVersion() {
        return false;
    }

    protected boolean supportsBatchDeleteTableVersions() {
        return false;
    }

    protected boolean supportsBatchCreateTableVersions() {
        return false;
    }

    protected boolean supportsBatchCommitTables() {
        return false;
    }

    protected boolean supportsMergeInsertIntoTable() {
        return false;
    }

    protected boolean supportsQueryTable() {
        return false;
    }

    protected boolean supportsRestoreTable() {
        return false;
    }

    protected boolean supportsUpdateTable() {
        return false;
    }

    protected boolean supportsUpdateTableTag() {
        return false;
    }

    protected boolean supportsDescribeTransaction() {
        return false;
    }

    protected boolean supportsAlterTransaction() {
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
        adapter.dropNamespace(new DropNamespaceRequest().id(namespaceId()).mode("purge"));
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
    void createTable() {
        assumeTrue(supportsCreateTable() && supportsDescribeTable());
        createTableEntity();
        var response = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(tableName, response.getTable());
    }

    @Test
    void listTables() {
        assumeTrue(supportsCreateTable() && supportsListTables());
        createTableEntity();
        var response = adapter.listTables(new ListTablesRequest().id(namespaceId()).limit(100));
        assertTrue(response.getTables().contains(tableName));
    }

    @Test
    void registerTable() {
        assumeTrue(supportsCreateNamespace() && supportsRegisterTable() && supportsDescribeTable());
        createNamespaceEntity();
        var expectedLocation = "s3://warehouse/" + namespaceName + "/" + tableName;
        var request = new RegisterTableRequest().id(tableId()).location(expectedLocation).mode("create");
        adapter.registerTable(request);
        var described = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(expectedLocation, described.getLocation());
    }

    @Test
    void describeTable() {
        assumeTrue(supportsCreateTable() && supportsDescribeTable());
        createTableEntity();
        var response = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(tableName, response.getTable());
    }

    @Test
    void tableExists() {
        assumeTrue(supportsCreateTable() && supportsTableExists());
        createTableEntity();
        var result = assertDoesNotThrow(() -> {
            adapter.tableExists(new TableExistsRequest().id(tableId()));
            return true;
        });
        assertEquals(true, result);
    }

    @Test
    void dropTable() {
        assumeTrue(supportsCreateTable() && supportsDropTable() && supportsListTables());
        createTableEntity();
        adapter.dropTable(new DropTableRequest().id(tableId()));
        var listed = adapter.listTables(new ListTablesRequest().id(namespaceId()).limit(100));
        assertFalse(listed.getTables().contains(tableName));
    }

    @Test
    void deregisterTable() {
        assumeTrue(supportsCreateTable() && supportsDeregisterTable() && supportsListTables());
        createTableEntity();
        adapter.deregisterTable(new DeregisterTableRequest().id(tableId()));
        var listed = adapter.listTables(new ListTablesRequest().id(namespaceId()).limit(100));
        assertFalse(listed.getTables().contains(tableName));
    }

    @Test
    void alterTableAddColumns() {
        assumeTrue(supportsCreateTable() && supportsAlterTableAddColumns() && supportsDescribeTable());
        createTableEntity();
        var alter = adapter.alterTableAddColumns(new AlterTableAddColumnsRequest().id(tableId()));
        var described = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(alter.getVersion(), described.getVersion());
    }

    @Test
    void alterTableAlterColumns() {
        assumeTrue(supportsCreateTable() && supportsAlterTableAlterColumns() && supportsDescribeTable());
        createTableEntity();
        var alter = adapter.alterTableAlterColumns(new AlterTableAlterColumnsRequest().id(tableId()));
        var described = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(alter.getVersion(), described.getVersion());
    }

    @Test
    void alterTableDropColumns() {
        assumeTrue(supportsCreateTable() && supportsAlterTableDropColumns() && supportsDescribeTable());
        createTableEntity();
        var alter = adapter.alterTableDropColumns(new AlterTableDropColumnsRequest().id(tableId()).columns(List.of("col_a")));
        var described = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals(alter.getVersion(), described.getVersion());
    }

    @Test
    void analyzeTableQueryPlan() {
        assumeTrue(supportsCreateTable() && supportsAnalyzeTableQueryPlan());
        createTableEntity();
        var request = new AnalyzeTableQueryPlanRequest().id(tableId()).filter("id > 0").prefilter(true).version(1L);
        var response = adapter.analyzeTableQueryPlan(request);
        assertEquals(true, !response.isBlank());
    }

    @Test
    void countTableRows() {
        assumeTrue(supportsCreateTable() && supportsCountTableRows());
        createTableEntity();
        var response = adapter.countTableRows(new CountTableRowsRequest().id(tableId()).version(1L));
        assertEquals(0L, response);
    }

    @Test
    void createTableIndex() {
        assumeTrue(supportsCreateTable() && supportsCreateTableIndex() && supportsListTableIndices());
        createTableEntity();
        adapter.createTableIndex(new CreateTableIndexRequest().id(tableId()).withPosition(true));
        var listed = adapter.listTableIndices(new ListTableIndicesRequest().id(tableId()).limit(100));
        assertEquals(1, listed.getIndexes().size());
    }

    @Test
    void createTableTag() {
        assumeTrue(supportsCreateTable() && supportsCreateTableTag() && supportsGetTableTagVersion());
        createTableEntity();
        adapter.createTableTag(new CreateTableTagRequest().id(tableId()).tag(tagName).version(1L));
        var version = adapter.getTableTagVersion(new GetTableTagVersionRequest().id(tableId()).tag(tagName));
        assertEquals(1L, version.getVersion());
    }

    @Test
    void deleteFromTable() {
        assumeTrue(supportsCreateTable() && supportsDeleteFromTable() && supportsCountTableRows());
        createTableEntity();
        adapter.deleteFromTable(new DeleteFromTableRequest().id(tableId()));
        var rows = adapter.countTableRows(new CountTableRowsRequest().id(tableId()));
        assertEquals(0L, rows);
    }

    @Test
    void deleteTableTag() {
        assumeTrue(supportsCreateTable() && supportsCreateTableTag() && supportsDeleteTableTag() && supportsListTableTags());
        createTableEntity();
        adapter.createTableTag(new CreateTableTagRequest().id(tableId()).tag(tagName).version(1L));
        adapter.deleteTableTag(new DeleteTableTagRequest().id(tableId()).tag(tagName));
        var listed = adapter.listTableTags(new ListTableTagsRequest().id(tableId()).limit(100));
        assertFalse(listed.getTags().containsKey(tagName));
    }

    @Test
    void describeTableIndexStats() {
        assumeTrue(supportsCreateTable() && supportsCreateTableIndex() && supportsDescribeTableIndexStats());
        createTableEntity();
        adapter.createTableIndex(new CreateTableIndexRequest().id(tableId()).withPosition(true));
        var stats = adapter.describeTableIndexStats(new DescribeTableIndexStatsRequest().id(tableId()), indexName);
        assertTrue(stats.getNumIndices() >= 1);
    }

    @Test
    void dropTableIndex() {
        assumeTrue(supportsCreateTable() && supportsCreateTableIndex() && supportsDropTableIndex() && supportsListTableIndices());
        createTableEntity();
        adapter.createTableIndex(new CreateTableIndexRequest().id(tableId()).withPosition(true));
        adapter.dropTableIndex(new DropTableIndexRequest().id(tableId()), indexName);
        var listed = adapter.listTableIndices(new ListTableIndicesRequest().id(tableId()).limit(100));
        assertEquals(0, listed.getIndexes().size());
    }

    @Test
    void explainTableQueryPlan() {
        assumeTrue(supportsCreateTable() && supportsExplainTableQueryPlan());
        createTableEntity();
        var query = new QueryTableRequest().id(tableId()).filter("id > 0").prefilter(true).version(1L);
        var response = adapter.explainTableQueryPlan(new ExplainTableQueryPlanRequest().id(tableId()).query(query));
        assertEquals(true, !response.isBlank());
    }

    @Test
    void getTableStats() {
        assumeTrue(supportsCreateTable() && supportsGetTableStats() && supportsCountTableRows());
        createTableEntity();
        var stats = adapter.getTableStats(new GetTableStatsRequest().id(tableId()));
        var rows = adapter.countTableRows(new CountTableRowsRequest().id(tableId()));
        assertEquals(rows, stats.getNumRows());
    }

    @Test
    void getTableTagVersion() {
        assumeTrue(supportsCreateTable() && supportsCreateTableTag() && supportsGetTableTagVersion());
        createTableEntity();
        adapter.createTableTag(new CreateTableTagRequest().id(tableId()).tag(tagName).version(1L));
        var version = adapter.getTableTagVersion(new GetTableTagVersionRequest().id(tableId()).tag(tagName));
        assertEquals(1L, version.getVersion());
    }

    @Test
    void insertIntoTable() {
        assumeTrue(supportsCreateTable() && supportsInsertIntoTable() && supportsCountTableRows());
        createTableEntity();
        adapter.insertIntoTable(new InsertIntoTableRequest().id(tableId()).mode("append"), requestData());
        var rows = adapter.countTableRows(new CountTableRowsRequest().id(tableId()));
        assertTrue(rows >= 0);
    }

    @Test
    void listTableIndices() {
        assumeTrue(supportsCreateTable() && supportsCreateTableIndex() && supportsListTableIndices());
        createTableEntity();
        adapter.createTableIndex(new CreateTableIndexRequest().id(tableId()).withPosition(true));
        var listed = adapter.listTableIndices(new ListTableIndicesRequest().id(tableId()).limit(100));
        assertEquals(1, listed.getIndexes().size());
    }

    @Test
    void listTableTags() {
        assumeTrue(supportsCreateTable() && supportsCreateTableTag() && supportsListTableTags());
        createTableEntity();
        adapter.createTableTag(new CreateTableTagRequest().id(tableId()).tag(tagName).version(1L));
        var listed = adapter.listTableTags(new ListTableTagsRequest().id(tableId()).limit(100));
        assertTrue(listed.getTags().containsKey(tagName));
    }

    @Test
    void listTableVersions() {
        assumeTrue(supportsCreateTableVersion() && supportsListTableVersions());
        var create = adapter.createTableVersion(new CreateTableVersionRequest().id(tableId()).version(1L));
        var listed = adapter.listTableVersions(new ListTableVersionsRequest().id(tableId()).limit(100));
        assertEquals(create.getVersion().getVersion(), listed.getVersions().get(0).getVersion());
    }

    @Test
    void createTableVersion() {
        assumeTrue(supportsCreateTableVersion() && supportsDescribeTableVersion());
        var created = adapter.createTableVersion(new CreateTableVersionRequest().id(tableId()).version(1L));
        var described = adapter.describeTableVersion(new DescribeTableVersionRequest().id(tableId()).version(1L));
        assertEquals(created.getVersion().getVersion(), described.getVersion().getVersion());
    }

    @Test
    void describeTableVersion() {
        assumeTrue(supportsCreateTableVersion() && supportsDescribeTableVersion());
        adapter.createTableVersion(new CreateTableVersionRequest().id(tableId()).version(1L));
        var described = adapter.describeTableVersion(new DescribeTableVersionRequest().id(tableId()).version(1L));
        assertEquals(1L, described.getVersion().getVersion());
    }

    @Test
    void batchDeleteTableVersions() {
        assumeTrue(supportsCreateTableVersion() && supportsBatchDeleteTableVersions() && supportsListTableVersions());
        adapter.createTableVersion(new CreateTableVersionRequest().id(tableId()).version(1L));
        adapter.batchDeleteTableVersions(new BatchDeleteTableVersionsRequest().id(tableId()));
        var listed = adapter.listTableVersions(new ListTableVersionsRequest().id(tableId()).limit(100));
        assertEquals(0, listed.getVersions().size());
    }

    @Test
    void batchCreateTableVersions() {
        assumeTrue(supportsBatchCreateTableVersions() && supportsListTableVersions());
        adapter.batchCreateTableVersions(new BatchCreateTableVersionsRequest().entries(List.of()));
        var listed = adapter.listTableVersions(new ListTableVersionsRequest().id(tableId()).limit(100));
        assertEquals(0, listed.getVersions().size());
    }

    @Test
    void batchCommitTables() {
        assumeTrue(supportsBatchCommitTables());
        var response = adapter.batchCommitTables(new BatchCommitTablesRequest().operations(List.of()));
        assertEquals(0, response.getResults().size());
    }

    @Test
    void mergeInsertIntoTable() {
        assumeTrue(supportsCreateTable() && supportsMergeInsertIntoTable() && supportsCountTableRows());
        createTableEntity();
        adapter.mergeInsertIntoTable(new MergeInsertIntoTableRequest().id(tableId()).on("source.id = target.id"), requestData());
        var rows = adapter.countTableRows(new CountTableRowsRequest().id(tableId()));
        assertTrue(rows >= 0);
    }

    @Test
    void queryTable() {
        assumeTrue(supportsCreateTable() && supportsQueryTable());
        createTableEntity();
        var response = adapter.queryTable(new QueryTableRequest().id(tableId()).filter("id > 0"));
        assertTrue(response.length >= 0);
    }

    @Test
    void restoreTable() {
        assumeTrue(supportsCreateTableVersion() && supportsRestoreTable() && supportsDescribeTableVersion());
        adapter.createTableVersion(new CreateTableVersionRequest().id(tableId()).version(1L));
        adapter.restoreTable(new RestoreTableRequest().id(tableId()).version(1L));
        var described = adapter.describeTableVersion(new DescribeTableVersionRequest().id(tableId()).version(1L));
        assertEquals(1L, described.getVersion().getVersion());
    }

    @Test
    void updateTable() {
        assumeTrue(supportsCreateTable() && supportsUpdateTable() && supportsDescribeTable());
        createTableEntity();
        adapter.updateTable(new UpdateTableRequest().id(tableId()).properties(Map.of("stage", "updated")));
        var described = adapter.describeTable(new DescribeTableRequest().id(tableId()));
        assertEquals("updated", described.getProperties().get("stage"));
    }

    @Test
    void updateTableTag() {
        assumeTrue(supportsCreateTable() && supportsCreateTableTag() && supportsUpdateTableTag() && supportsGetTableTagVersion());
        createTableEntity();
        adapter.createTableTag(new CreateTableTagRequest().id(tableId()).tag(tagName).version(1L));
        adapter.updateTableTag(new UpdateTableTagRequest().id(tableId()).tag(tagName).version(2L));
        var version = adapter.getTableTagVersion(new GetTableTagVersionRequest().id(tableId()).tag(tagName));
        assertEquals(2L, version.getVersion());
    }

    @Test
    void describeTransaction() {
        assumeTrue(supportsDescribeTransaction() && supportsAlterTransaction());
        adapter.alterTransaction(new AlterTransactionRequest().id(List.of(transactionName)).actions(List.of()));
        var response = adapter.describeTransaction(new DescribeTransactionRequest().id(List.of(transactionName)));
        assertEquals("committed", response.getStatus());
    }

    @Test
    void alterTransaction() {
        assumeTrue(supportsAlterTransaction() && supportsDescribeTransaction());
        adapter.alterTransaction(new AlterTransactionRequest().id(List.of(transactionName)).actions(List.of()));
        var described = adapter.describeTransaction(new DescribeTransactionRequest().id(List.of(transactionName)));
        assertEquals("committed", described.getStatus());
    }
}
