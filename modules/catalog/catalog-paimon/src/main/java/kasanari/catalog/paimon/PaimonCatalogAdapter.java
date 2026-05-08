package kasanari.catalog.paimon;


import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.rest.requests.AlterDatabaseRequest;
import org.apache.paimon.rest.requests.AlterFunctionRequest;
import org.apache.paimon.rest.requests.AlterTableRequest;
import org.apache.paimon.rest.requests.AlterViewRequest;
import org.apache.paimon.rest.requests.AuthTableQueryRequest;
import org.apache.paimon.rest.requests.CommitTableRequest;
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
import org.apache.paimon.rest.responses.AlterDatabaseResponse;
import org.apache.paimon.rest.responses.AuthTableQueryResponse;
import org.apache.paimon.rest.responses.CommitTableResponse;
import org.apache.paimon.rest.responses.GetDatabaseResponse;
import org.apache.paimon.rest.responses.GetFunctionResponse;
import org.apache.paimon.rest.responses.GetTableResponse;
import org.apache.paimon.rest.responses.GetTableSnapshotResponse;
import org.apache.paimon.rest.responses.GetTableTokenResponse;
import org.apache.paimon.rest.responses.GetTagResponse;
import org.apache.paimon.rest.responses.GetVersionSnapshotResponse;
import org.apache.paimon.rest.responses.GetViewResponse;
import org.apache.paimon.rest.responses.ListBranchesResponse;
import org.apache.paimon.rest.responses.ListConsumersResponse;
import org.apache.paimon.rest.responses.ListDatabasesResponse;
import org.apache.paimon.rest.responses.ListFunctionDetailsResponse;
import org.apache.paimon.rest.responses.ListFunctionsGloballyResponse;
import org.apache.paimon.rest.responses.ListFunctionsResponse;
import org.apache.paimon.rest.responses.ListPartitionsResponse;
import org.apache.paimon.rest.responses.ListSnapshotsResponse;
import org.apache.paimon.rest.responses.ListTableDetailsResponse;
import org.apache.paimon.rest.responses.ListTablesGloballyResponse;
import org.apache.paimon.rest.responses.ListTablesResponse;
import org.apache.paimon.rest.responses.ListTagsResponse;
import org.apache.paimon.rest.responses.ListViewDetailsResponse;
import org.apache.paimon.rest.responses.ListViewsGloballyResponse;
import org.apache.paimon.rest.responses.ListViewsResponse;

public interface PaimonCatalogAdapter {

    ListDatabasesResponse listDatabases(String prefix, Integer maxResults, String pageToken);

    void createDatabase(String prefix, CreateDatabaseRequest request);

    GetDatabaseResponse getDatabase(String prefix, String database);

    void dropDatabase(String prefix, String database);

    AlterDatabaseResponse alterDatabase(String prefix, String database, AlterDatabaseRequest request);

    void registerTable(String prefix, String database, RegisterTableRequest request);

    ListTablesResponse listTables(
            String prefix,
            String database,
            Integer maxResults,
            String pageToken,
            String tableNamePattern);

    void createTable(String prefix, String database, CreateTableRequest request);

    ListTableDetailsResponse listTableDetails(
            String prefix,
            String database,
            Integer maxResults,
            String pageToken,
            String tableNamePattern,
            String tableType);

    ListTablesGloballyResponse listTablesGlobally(
            String prefix,
            String databaseNamePattern,
            String tableNamePattern,
            Integer maxResults,
            String pageToken);

    GetTableResponse getTableById(String prefix, String tableId);

    GetTableResponse getTable(String prefix, String database, String table);

    void alterTable(String prefix, String database, String table, AlterTableRequest request);

    void dropTable(String prefix, String database, String table);

    void renameTable(String prefix, RenameTableRequest request);

    CommitTableResponse commitTable(String prefix, String database, String table, CommitTableRequest request);

    void rollbackTable(String prefix, String database, String table, RollbackTableRequest request);

    void rollbackSchema(String prefix, String database, String table, RollbackSchemaRequest request);

    GetTableTokenResponse getTableToken(String prefix, String database, String table);

    AuthTableQueryResponse authTableQuery(String prefix, String database, String table, AuthTableQueryRequest request);

    GetTableSnapshotResponse getTableSnapshot(String prefix, String database, String table);

    GetVersionSnapshotResponse getVersionSnapshot(String prefix, String database, String table, String version);

    ListSnapshotsResponse listSnapshots(
            String prefix,
            String database,
            String table,
            Integer maxResults,
            String pageToken);

    ListPartitionsResponse listPartitions(
            String prefix,
            String database,
            String table,
            Integer maxResults,
            String pageToken,
            String partitionNamePattern);

    void markDonePartitions(String prefix, String database, String table, MarkDonePartitionsRequest request);

    ListPartitionsResponse listPartitionsByNames(
            String prefix,
            String database,
            String table,
            ListPartitionsByNamesRequest request);

    ListBranchesResponse listBranches(String prefix, String database, String table);

    void createBranch(String prefix, String database, String table, CreateBranchRequest request);

    void dropBranch(String prefix, String database, String table, String branch);

    void renameBranch(String prefix, String database, String table, String branch, RenameBranchRequest request);

    void forwardBranch(String prefix, String database, String table, String branch, ForwardBranchRequest request);

    ListTagsResponse listTags(
            String prefix,
            String database,
            String table,
            Integer maxResults,
            String pageToken,
            String tagNamePrefix);

    void createTag(String prefix, String database, String table, CreateTagRequest request);

    GetTagResponse getTag(String prefix, String database, String table, String tag);

    void deleteTag(String prefix, String database, String table, String tag);

    ListConsumersResponse listConsumers(
            String prefix,
            String database,
            String table,
            Integer maxResults,
            String pageToken);

    void resetConsumer(String prefix, String database, String table, ResetConsumerRequest request);

    ListViewsResponse listViews(
            String prefix,
            String database,
            Integer maxResults,
            String pageToken,
            String viewNamePattern);

    void createView(String prefix, String database, CreateViewRequest request);

    ListViewDetailsResponse listViewDetails(
            String prefix,
            String database,
            Integer maxResults,
            String pageToken,
            String viewNamePattern);

    ListViewsGloballyResponse listViewsGlobally(
            String prefix,
            String databaseNamePattern,
            String viewNamePattern,
            Integer maxResults,
            String pageToken);

    GetViewResponse getView(String prefix, String database, String view);

    void alterView(String prefix, String database, String view, AlterViewRequest request);

    void dropView(String prefix, String database, String view);

    void renameView(String prefix, RenameTableRequest request);

    ListFunctionsResponse listFunctions(
            String prefix,
            String database,
            Integer maxResults,
            String pageToken,
            String functionNamePattern);

    void createFunction(String prefix, String database, CreateFunctionRequest request);

    ListFunctionDetailsResponse listFunctionDetails(
            String prefix,
            String database,
            Integer maxResults,
            String pageToken,
            String functionNamePattern);

    ListFunctionsGloballyResponse listFunctionsGlobally(
            String prefix,
            String databaseNamePattern,
            String functionNamePattern,
            Integer maxResults,
            String pageToken);

    GetFunctionResponse getFunction(String prefix, String database, String function);

    void alterFunction(String prefix, String database, String function, AlterFunctionRequest request);

    void dropFunction(String prefix, String database, String function);

    // for testing only
    Catalog getUnderlyingCatalog();
}
