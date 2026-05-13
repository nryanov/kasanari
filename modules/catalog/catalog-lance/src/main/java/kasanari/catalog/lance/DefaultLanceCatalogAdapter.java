package kasanari.catalog.lance;

import org.lance.namespace.LanceNamespace;
import org.lance.namespace.model.AlterTableAddColumnsRequest;
import org.lance.namespace.model.AlterTableAddColumnsResponse;
import org.lance.namespace.model.AlterTableAlterColumnsRequest;
import org.lance.namespace.model.AlterTableAlterColumnsResponse;
import org.lance.namespace.model.AlterTableDropColumnsRequest;
import org.lance.namespace.model.AlterTableDropColumnsResponse;
import org.lance.namespace.model.AlterTransactionRequest;
import org.lance.namespace.model.AlterTransactionResponse;
import org.lance.namespace.model.AnalyzeTableQueryPlanRequest;
import org.lance.namespace.model.BatchCommitTablesRequest;
import org.lance.namespace.model.BatchCommitTablesResponse;
import org.lance.namespace.model.BatchCreateTableVersionsRequest;
import org.lance.namespace.model.BatchCreateTableVersionsResponse;
import org.lance.namespace.model.BatchDeleteTableVersionsRequest;
import org.lance.namespace.model.BatchDeleteTableVersionsResponse;
import org.lance.namespace.model.CountTableRowsRequest;
import org.lance.namespace.model.CreateNamespaceRequest;
import org.lance.namespace.model.CreateNamespaceResponse;
import org.lance.namespace.model.CreateTableIndexRequest;
import org.lance.namespace.model.CreateTableIndexResponse;
import org.lance.namespace.model.CreateTableRequest;
import org.lance.namespace.model.CreateTableResponse;
import org.lance.namespace.model.CreateTableTagRequest;
import org.lance.namespace.model.CreateTableTagResponse;
import org.lance.namespace.model.CreateTableVersionRequest;
import org.lance.namespace.model.CreateTableVersionResponse;
import org.lance.namespace.model.DeleteFromTableRequest;
import org.lance.namespace.model.DeleteFromTableResponse;
import org.lance.namespace.model.DeleteTableTagRequest;
import org.lance.namespace.model.DeleteTableTagResponse;
import org.lance.namespace.model.DeregisterTableRequest;
import org.lance.namespace.model.DeregisterTableResponse;
import org.lance.namespace.model.DescribeNamespaceRequest;
import org.lance.namespace.model.DescribeNamespaceResponse;
import org.lance.namespace.model.DescribeTableIndexStatsRequest;
import org.lance.namespace.model.DescribeTableIndexStatsResponse;
import org.lance.namespace.model.DescribeTableRequest;
import org.lance.namespace.model.DescribeTableResponse;
import org.lance.namespace.model.DescribeTableVersionRequest;
import org.lance.namespace.model.DescribeTableVersionResponse;
import org.lance.namespace.model.DescribeTransactionRequest;
import org.lance.namespace.model.DescribeTransactionResponse;
import org.lance.namespace.model.DropNamespaceRequest;
import org.lance.namespace.model.DropNamespaceResponse;
import org.lance.namespace.model.DropTableIndexRequest;
import org.lance.namespace.model.DropTableIndexResponse;
import org.lance.namespace.model.DropTableRequest;
import org.lance.namespace.model.DropTableResponse;
import org.lance.namespace.model.ExplainTableQueryPlanRequest;
import org.lance.namespace.model.GetTableStatsRequest;
import org.lance.namespace.model.GetTableStatsResponse;
import org.lance.namespace.model.GetTableTagVersionRequest;
import org.lance.namespace.model.GetTableTagVersionResponse;
import org.lance.namespace.model.InsertIntoTableRequest;
import org.lance.namespace.model.InsertIntoTableResponse;
import org.lance.namespace.model.ListNamespacesRequest;
import org.lance.namespace.model.ListNamespacesResponse;
import org.lance.namespace.model.ListTableIndicesRequest;
import org.lance.namespace.model.ListTableIndicesResponse;
import org.lance.namespace.model.ListTableTagsRequest;
import org.lance.namespace.model.ListTableTagsResponse;
import org.lance.namespace.model.ListTableVersionsRequest;
import org.lance.namespace.model.ListTableVersionsResponse;
import org.lance.namespace.model.ListTablesRequest;
import org.lance.namespace.model.ListTablesResponse;
import org.lance.namespace.model.MergeInsertIntoTableRequest;
import org.lance.namespace.model.MergeInsertIntoTableResponse;
import org.lance.namespace.model.NamespaceExistsRequest;
import org.lance.namespace.model.QueryTableRequest;
import org.lance.namespace.model.RegisterTableRequest;
import org.lance.namespace.model.RegisterTableResponse;
import org.lance.namespace.model.RestoreTableRequest;
import org.lance.namespace.model.RestoreTableResponse;
import org.lance.namespace.model.TableExistsRequest;
import org.lance.namespace.model.UpdateTableRequest;
import org.lance.namespace.model.UpdateTableResponse;
import org.lance.namespace.model.UpdateTableTagRequest;
import org.lance.namespace.model.UpdateTableTagResponse;

public class DefaultLanceCatalogAdapter implements LanceCatalogAdapter {
    private final LanceNamespace namespace;

    public DefaultLanceCatalogAdapter(LanceNamespace namespace) {
        this.namespace = namespace;
    }

    @Override
    public CreateNamespaceResponse createNamespace(CreateNamespaceRequest request) {
        return namespace.createNamespace(request);
    }

    @Override
    public DescribeNamespaceResponse describeNamespace(DescribeNamespaceRequest request) {
        return namespace.describeNamespace(request);
    }

    @Override
    public DropNamespaceResponse dropNamespace(DropNamespaceRequest request) {
        return namespace.dropNamespace(request);
    }

    @Override
    public void namespaceExists(NamespaceExistsRequest request) {
        namespace.namespaceExists(request);
    }

    @Override
    public ListNamespacesResponse listNamespaces(ListNamespacesRequest request) {
        return namespace.listNamespaces(request);
    }

    @Override
    public ListTablesResponse listTables(ListTablesRequest request) {
        return namespace.listTables(request);
    }

    @Override
    public RegisterTableResponse registerTable(RegisterTableRequest request) {
        return namespace.registerTable(request);
    }

    @Override
    public DescribeTableResponse describeTable(DescribeTableRequest request) {
        return namespace.describeTable(request);
    }

    @Override
    public void tableExists(TableExistsRequest request) {
        namespace.tableExists(request);
    }

    @Override
    public DropTableResponse dropTable(DropTableRequest request) {
        return namespace.dropTable(request);
    }

    @Override
    public DeregisterTableResponse deregisterTable(DeregisterTableRequest request) {
        return namespace.deregisterTable(request);
    }

    @Override
    public AlterTableAddColumnsResponse alterTableAddColumns(AlterTableAddColumnsRequest request) {
        return namespace.alterTableAddColumns(request);
    }

    @Override
    public AlterTableAlterColumnsResponse alterTableAlterColumns(AlterTableAlterColumnsRequest request) {
        return namespace.alterTableAlterColumns(request);
    }

    @Override
    public AlterTableDropColumnsResponse alterTableDropColumns(AlterTableDropColumnsRequest request) {
        return namespace.alterTableDropColumns(request);
    }

    @Override
    public String analyzeTableQueryPlan(AnalyzeTableQueryPlanRequest request) {
        return namespace.analyzeTableQueryPlan(request);
    }

    @Override
    public Long countTableRows(CountTableRowsRequest request) {
        return namespace.countTableRows(request);
    }

    @Override
    public CreateTableResponse createTable(CreateTableRequest request, byte[] requestData) {
        return namespace.createTable(request, requestData);
    }

    @Override
    public CreateTableIndexResponse createTableIndex(CreateTableIndexRequest request) {
        return namespace.createTableIndex(request);
    }

    @Override
    public CreateTableTagResponse createTableTag(CreateTableTagRequest request) {
        return namespace.createTableTag(request);
    }

    @Override
    public DeleteFromTableResponse deleteFromTable(DeleteFromTableRequest request) {
        return namespace.deleteFromTable(request);
    }

    @Override
    public DeleteTableTagResponse deleteTableTag(DeleteTableTagRequest request) {
        return namespace.deleteTableTag(request);
    }

    @Override
    public DescribeTableIndexStatsResponse describeTableIndexStats(
            DescribeTableIndexStatsRequest request, String indexName) {
        return namespace.describeTableIndexStats(request, indexName);
    }

    @Override
    public DropTableIndexResponse dropTableIndex(DropTableIndexRequest request, String indexName) {
        return namespace.dropTableIndex(request, indexName);
    }

    @Override
    public String explainTableQueryPlan(ExplainTableQueryPlanRequest request) {
        return namespace.explainTableQueryPlan(request);
    }

    @Override
    public GetTableStatsResponse getTableStats(GetTableStatsRequest request) {
        return namespace.getTableStats(request);
    }

    @Override
    public GetTableTagVersionResponse getTableTagVersion(GetTableTagVersionRequest request) {
        return namespace.getTableTagVersion(request);
    }

    @Override
    public InsertIntoTableResponse insertIntoTable(InsertIntoTableRequest request, byte[] requestData) {
        return namespace.insertIntoTable(request, requestData);
    }

    @Override
    public ListTableIndicesResponse listTableIndices(ListTableIndicesRequest request) {
        return namespace.listTableIndices(request);
    }

    @Override
    public ListTableTagsResponse listTableTags(ListTableTagsRequest request) {
        return namespace.listTableTags(request);
    }

    @Override
    public ListTableVersionsResponse listTableVersions(ListTableVersionsRequest request) {
        return namespace.listTableVersions(request);
    }

    @Override
    public CreateTableVersionResponse createTableVersion(CreateTableVersionRequest request) {
        return namespace.createTableVersion(request);
    }

    @Override
    public DescribeTableVersionResponse describeTableVersion(DescribeTableVersionRequest request) {
        return namespace.describeTableVersion(request);
    }

    @Override
    public BatchDeleteTableVersionsResponse batchDeleteTableVersions(BatchDeleteTableVersionsRequest request) {
        return namespace.batchDeleteTableVersions(request);
    }

    @Override
    public BatchCreateTableVersionsResponse batchCreateTableVersions(BatchCreateTableVersionsRequest request) {
        return namespace.batchCreateTableVersions(request);
    }

    @Override
    public BatchCommitTablesResponse batchCommitTables(BatchCommitTablesRequest request) {
        return namespace.batchCommitTables(request);
    }

    @Override
    public MergeInsertIntoTableResponse mergeInsertIntoTable(MergeInsertIntoTableRequest request, byte[] requestData) {
        return namespace.mergeInsertIntoTable(request, requestData);
    }

    @Override
    public byte[] queryTable(QueryTableRequest request) {
        return namespace.queryTable(request);
    }

    @Override
    public RestoreTableResponse restoreTable(RestoreTableRequest request) {
        return namespace.restoreTable(request);
    }

    @Override
    public UpdateTableResponse updateTable(UpdateTableRequest request) {
        return namespace.updateTable(request);
    }

    @Override
    public UpdateTableTagResponse updateTableTag(UpdateTableTagRequest request) {
        return namespace.updateTableTag(request);
    }

    @Override
    public DescribeTransactionResponse describeTransaction(DescribeTransactionRequest request) {
        return namespace.describeTransaction(request);
    }

    @Override
    public AlterTransactionResponse alterTransaction(AlterTransactionRequest request) {
        return namespace.alterTransaction(request);
    }

    @Override
    public LanceNamespace delegate() {
        return namespace;
    }

    @Override
    public void close() throws Exception {
        if (namespace instanceof AutoCloseable closeable) {
            closeable.close();
        }
    }
}
