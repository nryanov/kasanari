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

public class DefaultPaimonCatalogAdapter implements PaimonCatalogAdapter {
    private final Catalog catalog;

    public DefaultPaimonCatalogAdapter(Catalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public ListDatabasesResponse listDatabases(String prefix, Integer maxResults, String pageToken) {
        return null;
    }

    @Override
    public void createDatabase(String prefix, CreateDatabaseRequest request) {

    }

    @Override
    public GetDatabaseResponse getDatabase(String prefix, String database) {
        return null;
    }

    @Override
    public void dropDatabase(String prefix, String database) {

    }

    @Override
    public AlterDatabaseResponse alterDatabase(String prefix, String database, AlterDatabaseRequest request) {
        return null;
    }

    @Override
    public void registerTable(String prefix, String database, RegisterTableRequest request) {

    }

    @Override
    public ListTablesResponse listTables(String prefix, String database, Integer maxResults, String pageToken, String tableNamePattern) {
        return null;
    }

    @Override
    public void createTable(String prefix, String database, CreateTableRequest request) {
    }

    @Override
    public ListTableDetailsResponse listTableDetails(String prefix, String database, Integer maxResults, String pageToken, String tableNamePattern, String tableType) {
        return null;
    }

    @Override
    public ListTablesGloballyResponse listTablesGlobally(String prefix, String databaseNamePattern, String tableNamePattern, Integer maxResults, String pageToken) {
        return null;
    }

    @Override
    public GetTableResponse getTableById(String prefix, String tableId) {
        return null;
    }

    @Override
    public GetTableResponse getTable(String prefix, String database, String table) {
        return null;
    }

    @Override
    public void alterTable(String prefix, String database, String table, AlterTableRequest request) {

    }

    @Override
    public void dropTable(String prefix, String database, String table) {

    }

    @Override
    public void renameTable(String prefix, RenameTableRequest request) {

    }

    @Override
    public CommitTableResponse commitTable(String prefix, String database, String table, CommitTableRequest request) {
        return null;
    }

    @Override
    public void rollbackTable(String prefix, String database, String table, RollbackTableRequest request) {

    }

    @Override
    public void rollbackSchema(String prefix, String database, String table, RollbackSchemaRequest request) {

    }

    @Override
    public GetTableTokenResponse getTableToken(String prefix, String database, String table) {
        return null;
    }

    @Override
    public AuthTableQueryResponse authTableQuery(String prefix, String database, String table, AuthTableQueryRequest request) {
        return null;
    }

    @Override
    public GetTableSnapshotResponse getTableSnapshot(String prefix, String database, String table) {
        return null;
    }

    @Override
    public GetVersionSnapshotResponse getVersionSnapshot(String prefix, String database, String table, String version) {
        return null;
    }

    @Override
    public ListSnapshotsResponse listSnapshots(String prefix, String database, String table, Integer maxResults, String pageToken) {
        return null;
    }

    @Override
    public ListPartitionsResponse listPartitions(String prefix, String database, String table, Integer maxResults, String pageToken, String partitionNamePattern) {
        return null;
    }

    @Override
    public void markDonePartitions(String prefix, String database, String table, MarkDonePartitionsRequest request) {

    }

    @Override
    public ListPartitionsResponse listPartitionsByNames(String prefix, String database, String table, ListPartitionsByNamesRequest request) {
        return null;
    }

    @Override
    public ListBranchesResponse listBranches(String prefix, String database, String table) {
        return null;
    }

    @Override
    public void createBranch(String prefix, String database, String table, CreateBranchRequest request) {

    }

    @Override
    public void dropBranch(String prefix, String database, String table, String branch) {

    }

    @Override
    public void renameBranch(String prefix, String database, String table, String branch, RenameBranchRequest request) {

    }

    @Override
    public void forwardBranch(String prefix, String database, String table, String branch, ForwardBranchRequest request) {

    }

    @Override
    public ListTagsResponse listTags(String prefix, String database, String table, Integer maxResults, String pageToken, String tagNamePrefix) {
        return null;
    }

    @Override
    public void createTag(String prefix, String database, String table, CreateTagRequest request) {

    }

    @Override
    public GetTagResponse getTag(String prefix, String database, String table, String tag) {
        return null;
    }

    @Override
    public void deleteTag(String prefix, String database, String table, String tag) {

    }

    @Override
    public ListConsumersResponse listConsumers(String prefix, String database, String table, Integer maxResults, String pageToken) {
        return null;
    }

    @Override
    public void resetConsumer(String prefix, String database, String table, ResetConsumerRequest request) {

    }

    @Override
    public ListViewsResponse listViews(String prefix, String database, Integer maxResults, String pageToken, String viewNamePattern) {
        return null;
    }

    @Override
    public void createView(String prefix, String database, CreateViewRequest request) {

    }

    @Override
    public ListViewDetailsResponse listViewDetails(String prefix, String database, Integer maxResults, String pageToken, String viewNamePattern) {
        return null;
    }

    @Override
    public ListViewsGloballyResponse listViewsGlobally(String prefix, String databaseNamePattern, String viewNamePattern, Integer maxResults, String pageToken) {
        return null;
    }

    @Override
    public GetViewResponse getView(String prefix, String database, String view) {
        return null;
    }

    @Override
    public void alterView(String prefix, String database, String view, AlterViewRequest request) {

    }

    @Override
    public void dropView(String prefix, String database, String view) {

    }

    @Override
    public void renameView(String prefix, RenameTableRequest request) {

    }

    @Override
    public ListFunctionsResponse listFunctions(String prefix, String database, Integer maxResults, String pageToken, String functionNamePattern) {
        return null;
    }

    @Override
    public void createFunction(String prefix, String database, CreateFunctionRequest request) {

    }

    @Override
    public ListFunctionDetailsResponse listFunctionDetails(String prefix, String database, Integer maxResults, String pageToken, String functionNamePattern) {
        return null;
    }

    @Override
    public ListFunctionsGloballyResponse listFunctionsGlobally(String prefix, String databaseNamePattern, String functionNamePattern, Integer maxResults, String pageToken) {
        return null;
    }

    @Override
    public GetFunctionResponse getFunction(String prefix, String database, String function) {
        return null;
    }

    @Override
    public void alterFunction(String prefix, String database, String function, AlterFunctionRequest request) {

    }

    @Override
    public void dropFunction(String prefix, String database, String function) {

    }
}
