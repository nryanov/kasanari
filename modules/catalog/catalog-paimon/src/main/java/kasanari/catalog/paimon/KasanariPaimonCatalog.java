package kasanari.catalog.paimon;

import org.apache.paimon.PagedList;
import org.apache.paimon.Snapshot;
import org.apache.paimon.catalog.AbstractCatalog;
import org.apache.paimon.catalog.CatalogLoader;
import org.apache.paimon.catalog.Database;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.catalog.PropertyChange;
import org.apache.paimon.fs.FileIO;
import org.apache.paimon.fs.Path;
import org.apache.paimon.function.Function;
import org.apache.paimon.function.FunctionChange;
import org.apache.paimon.partition.PartitionStatistics;
import org.apache.paimon.rest.responses.GetTagResponse;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.schema.SchemaChange;
import org.apache.paimon.schema.TableSchema;
import org.apache.paimon.table.Table;
import org.apache.paimon.utils.SnapshotNotExistException;
import org.apache.paimon.view.View;
import org.apache.paimon.view.ViewChange;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class KasanariPaimonCatalog extends AbstractCatalog {
    public KasanariPaimonCatalog(FileIO fileIO) {
        super(fileIO);
    }

    @Override
    public String warehouse() {
        return "";
    }

    @Override
    protected Database getDatabaseImpl(String name) throws DatabaseNotExistException {
        return null;
    }

    @Override
    protected void createDatabaseImpl(String name, Map<String, String> properties) {

    }

    @Override
    protected void dropDatabaseImpl(String name) {

    }

    @Override
    protected void alterDatabaseImpl(String name, List<PropertyChange> changes) throws DatabaseNotExistException {

    }

    @Override
    protected List<String> listTablesImpl(String databaseName) {
        return List.of();
    }

    @Override
    protected void dropTableImpl(Identifier identifier, List<Path> externalPaths) {

    }

    @Override
    protected void createTableImpl(Identifier identifier, Schema schema) {

    }

    @Override
    protected void renameTableImpl(Identifier fromTable, Identifier toTable) {

    }

    @Override
    protected void alterTableImpl(Identifier identifier, List<SchemaChange> changes) throws TableNotExistException, ColumnAlreadyExistException, ColumnNotExistException {

    }

    @Override
    protected TableSchema loadTableSchema(Identifier identifier) throws TableNotExistException {
        return null;
    }

    @Override
    public Table getTableById(String tableId) throws TableIdNotExistException {
        return super.getTableById(tableId);
    }

    @Override
    public List<String> listDatabases() {
        return List.of();
    }

    @Override
    public View getView(Identifier identifier) throws ViewNotExistException {
        return super.getView(identifier);
    }

    @Override
    public void dropView(Identifier identifier, boolean ignoreIfNotExists) throws ViewNotExistException {
        super.dropView(identifier, ignoreIfNotExists);
    }

    @Override
    public void createView(Identifier identifier, View view, boolean ignoreIfExists) throws ViewAlreadyExistException, DatabaseNotExistException {
        super.createView(identifier, view, ignoreIfExists);
    }

    @Override
    public List<String> listViews(String databaseName) throws DatabaseNotExistException {
        return super.listViews(databaseName);
    }

    @Override
    public PagedList<String> listViewsPaged(String databaseName, @Nullable Integer maxResults, @Nullable String pageToken, @Nullable String viewNamePattern) throws DatabaseNotExistException {
        return super.listViewsPaged(databaseName, maxResults, pageToken, viewNamePattern);
    }

    @Override
    public PagedList<View> listViewDetailsPaged(String databaseName, @Nullable Integer maxResults, @Nullable String pageToken, @Nullable String viewNamePattern) throws DatabaseNotExistException {
        return super.listViewDetailsPaged(databaseName, maxResults, pageToken, viewNamePattern);
    }

    @Override
    public PagedList<Identifier> listViewsPagedGlobally(@Nullable String databaseNamePattern, @Nullable String viewNamePattern, @Nullable Integer maxResults, @Nullable String pageToken) {
        return super.listViewsPagedGlobally(databaseNamePattern, viewNamePattern, maxResults, pageToken);
    }

    @Override
    public void renameView(Identifier fromView, Identifier toView, boolean ignoreIfNotExists) throws ViewNotExistException, ViewAlreadyExistException {
        super.renameView(fromView, toView, ignoreIfNotExists);
    }

    @Override
    public void alterView(Identifier view, List<ViewChange> viewChanges, boolean ignoreIfNotExists) throws ViewNotExistException, DialectAlreadyExistException, DialectNotExistException {
        super.alterView(view, viewChanges, ignoreIfNotExists);
    }

    @Override
    public List<String> listFunctions(String databaseName) {
        return super.listFunctions(databaseName);
    }

    @Override
    public Function getFunction(Identifier identifier) throws FunctionNotExistException {
        return super.getFunction(identifier);
    }

    @Override
    public void createFunction(Identifier identifier, Function function, boolean ignoreIfExists) throws FunctionAlreadyExistException, DatabaseNotExistException {
        super.createFunction(identifier, function, ignoreIfExists);
    }

    @Override
    public void dropFunction(Identifier identifier, boolean ignoreIfNotExists) throws FunctionNotExistException {
        super.dropFunction(identifier, ignoreIfNotExists);
    }

    @Override
    public void alterFunction(Identifier identifier, List<FunctionChange> changes, boolean ignoreIfNotExists) throws FunctionNotExistException, DefinitionAlreadyExistException, DefinitionNotExistException {
        super.alterFunction(identifier, changes, ignoreIfNotExists);
    }

    @Override
    public boolean commitSnapshot(Identifier identifier, @Nullable String tableUuid, Snapshot snapshot, List<PartitionStatistics> statistics) {
        return super.commitSnapshot(identifier, tableUuid, snapshot, statistics);
    }

    @Override
    public void createBranch(Identifier identifier, String branch, @Nullable String fromTag) throws TableNotExistException, BranchAlreadyExistException, TagNotExistException {
        super.createBranch(identifier, branch, fromTag);
    }

    @Override
    public void dropBranch(Identifier identifier, String branch) throws BranchNotExistException {
        super.dropBranch(identifier, branch);
    }

    @Override
    public void renameBranch(Identifier identifier, String fromBranch, String toBranch) throws BranchNotExistException, BranchAlreadyExistException {
        super.renameBranch(identifier, fromBranch, toBranch);
    }

    @Override
    public void fastForward(Identifier identifier, String branch) throws BranchNotExistException {
        super.fastForward(identifier, branch);
    }

    @Override
    public List<String> listBranches(Identifier identifier) throws TableNotExistException {
        return super.listBranches(identifier);
    }

    @Override
    public GetTagResponse getTag(Identifier identifier, String tagName) throws TableNotExistException, TagNotExistException {
        return super.getTag(identifier, tagName);
    }

    @Override
    public void createTag(Identifier identifier, String tagName, @Nullable Long snapshotId, @Nullable String timeRetained, boolean ignoreIfExists) throws TableNotExistException, SnapshotNotExistException, TagAlreadyExistException {
        super.createTag(identifier, tagName, snapshotId, timeRetained, ignoreIfExists);
    }

    @Override
    public PagedList<String> listTagsPaged(Identifier identifier, @Nullable Integer maxResults, @Nullable String pageToken, @Nullable String tagNamePrefix) throws TableNotExistException {
        return super.listTagsPaged(identifier, maxResults, pageToken, tagNamePrefix);
    }

    @Override
    public void deleteTag(Identifier identifier, String tagName) throws TableNotExistException, TagNotExistException {
        super.deleteTag(identifier, tagName);
    }

    @Override
    public CatalogLoader catalogLoader() {
        return null;
    }

    @Override
    public boolean caseSensitive() {
        return false;
    }

    @Override
    public void close() throws Exception {

    }
}
