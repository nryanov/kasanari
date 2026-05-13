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

public interface LanceCatalogAdapter extends AutoCloseable {
    CreateNamespaceResponse createNamespace(CreateNamespaceRequest request);

    DescribeNamespaceResponse describeNamespace(DescribeNamespaceRequest request);

    DropNamespaceResponse dropNamespace(DropNamespaceRequest request);

    void namespaceExists(NamespaceExistsRequest request);

    ListNamespacesResponse listNamespaces(ListNamespacesRequest request);

    ListTablesResponse listTables(ListTablesRequest request);

    RegisterTableResponse registerTable(RegisterTableRequest request);

    DescribeTableResponse describeTable(DescribeTableRequest request);

    void tableExists(TableExistsRequest request);

    DropTableResponse dropTable(DropTableRequest request);

    DeregisterTableResponse deregisterTable(DeregisterTableRequest request);

    AlterTableAddColumnsResponse alterTableAddColumns(AlterTableAddColumnsRequest request);

    AlterTableAlterColumnsResponse alterTableAlterColumns(AlterTableAlterColumnsRequest request);

    AlterTableDropColumnsResponse alterTableDropColumns(AlterTableDropColumnsRequest request);

    String analyzeTableQueryPlan(AnalyzeTableQueryPlanRequest request);

    Long countTableRows(CountTableRowsRequest request);

    CreateTableResponse createTable(CreateTableRequest request, byte[] requestData);

    CreateTableIndexResponse createTableIndex(CreateTableIndexRequest request);

    CreateTableTagResponse createTableTag(CreateTableTagRequest request);

    DeleteFromTableResponse deleteFromTable(DeleteFromTableRequest request);

    DeleteTableTagResponse deleteTableTag(DeleteTableTagRequest request);

    DescribeTableIndexStatsResponse describeTableIndexStats(DescribeTableIndexStatsRequest request, String indexName);

    DropTableIndexResponse dropTableIndex(DropTableIndexRequest request, String indexName);

    String explainTableQueryPlan(ExplainTableQueryPlanRequest request);

    GetTableStatsResponse getTableStats(GetTableStatsRequest request);

    GetTableTagVersionResponse getTableTagVersion(GetTableTagVersionRequest request);

    InsertIntoTableResponse insertIntoTable(InsertIntoTableRequest request, byte[] requestData);

    ListTableIndicesResponse listTableIndices(ListTableIndicesRequest request);

    ListTableTagsResponse listTableTags(ListTableTagsRequest request);

    ListTableVersionsResponse listTableVersions(ListTableVersionsRequest request);

    CreateTableVersionResponse createTableVersion(CreateTableVersionRequest request);

    DescribeTableVersionResponse describeTableVersion(DescribeTableVersionRequest request);

    BatchDeleteTableVersionsResponse batchDeleteTableVersions(BatchDeleteTableVersionsRequest request);

    BatchCreateTableVersionsResponse batchCreateTableVersions(BatchCreateTableVersionsRequest request);

    BatchCommitTablesResponse batchCommitTables(BatchCommitTablesRequest request);

    MergeInsertIntoTableResponse mergeInsertIntoTable(MergeInsertIntoTableRequest request, byte[] requestData);

    byte[] queryTable(QueryTableRequest request);

    RestoreTableResponse restoreTable(RestoreTableRequest request);

    UpdateTableResponse updateTable(UpdateTableRequest request);

    UpdateTableTagResponse updateTableTag(UpdateTableTagRequest request);

    DescribeTransactionResponse describeTransaction(DescribeTransactionRequest request);

    AlterTransactionResponse alterTransaction(AlterTransactionRequest request);

    LanceNamespace delegate();
}
