package kasanari.catalog.paimon;

import kasanari.catalog.paimon.model.DatabaseRecord;
import kasanari.catalog.paimon.model.TableRecord;
import kasanari.catalog.paimon.repository.BranchRepository;
import kasanari.catalog.paimon.repository.DatabaseRepository;
import kasanari.catalog.paimon.repository.FunctionRepository;
import kasanari.catalog.paimon.repository.TableRepository;
import kasanari.catalog.paimon.repository.TagRepository;
import kasanari.catalog.paimon.repository.TransactionManager;
import kasanari.catalog.paimon.repository.ViewRepository;
import org.apache.paimon.PagedList;
import org.apache.paimon.Snapshot;
import org.apache.paimon.catalog.AbstractCatalog;
import org.apache.paimon.catalog.CatalogContext;
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
import org.apache.paimon.schema.SchemaManager;
import org.apache.paimon.schema.TableSchema;
import org.apache.paimon.table.Table;
import org.apache.paimon.utils.SnapshotNotExistException;
import org.apache.paimon.view.View;
import org.apache.paimon.view.ViewChange;
import org.jdbi.v3.core.Handle;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KasanariPaimonCatalog extends AbstractCatalog {
    private final TransactionManager<Handle> transactionManager;
    private final DatabaseRepository<Handle> databaseRepository;
    private final TableRepository<Handle> tableRepository;
    private final ViewRepository<Handle> viewRepository;
    private final FunctionRepository<Handle> functionRepository;
    private final TagRepository<Handle> tagRepository;
    private final BranchRepository<Handle> branchRepository;

    private final FileIO fileIO;
    private final String catalogKey;
    private final String warehouse;

    public KasanariPaimonCatalog(FileIO fileIO, String catalogKey, CatalogContext context, String warehouse) {
        super(fileIO, context);

        this.transactionManager = null;
        this.databaseRepository = null;
        this.tableRepository = null;
        this.viewRepository = null;
        this.functionRepository = null;
        this.tagRepository = null;
        this.branchRepository = null;

        this.fileIO = fileIO;
        this.catalogKey = catalogKey;
        this.warehouse = warehouse;
    }

    @Override
    public String warehouse() {
        return warehouse;
    }

    @Override
    protected Database getDatabaseImpl(String name) throws DatabaseNotExistException {
        var maybe = transactionManager.inTransactionR(tx -> databaseRepository.findByName(tx, name));
        if (maybe.isEmpty()) {
            throw new DatabaseNotExistException(name);
        }

        var db = maybe.get();
        return Database.of(db.name(), db.options(), db.comment().orElse(null));
    }

    @Override
    protected void createDatabaseImpl(String name, Map<String, String> properties) {
        var db = new DatabaseRecord(name, properties, Optional.empty());
        transactionManager.inTransaction(tx -> databaseRepository.create(tx, db));
    }

    @Override
    protected void dropDatabaseImpl(String name) {
        transactionManager.inTransaction(tx -> databaseRepository.delete(tx, name));
    }

    @Override
    protected void alterDatabaseImpl(String name, List<PropertyChange> changes) throws DatabaseNotExistException {
        var updates = new HashMap<String, String>();
        var deletes = new HashSet<String>();

        changes.forEach(it -> {
            switch (it) {
                case PropertyChange.SetProperty i -> updates.put(i.property(), i.value());
                case PropertyChange.RemoveProperty i -> deletes.add(i.property());
                default -> {}
            }
        });

        var changed = transactionManager.inTransactionR(tx -> databaseRepository.alter(tx, name, updates, deletes));

        if (!changed) {
            throw new DatabaseNotExistException(name);
        }
    }

    @Override
    public List<String> listDatabases() {
        var list = transactionManager.inTransactionR(databaseRepository::findAll);

        return list.stream().map(DatabaseRecord::name).toList();
    }

    @Override
    protected List<String> listTablesImpl(String databaseName) {
        var list = transactionManager.inTransactionR(tx -> tableRepository.findAll(tx, databaseName));

        return list.stream().map(TableRecord::name).toList();
    }

    @Override
    protected void dropTableImpl(Identifier identifier, List<Path> externalPaths) {
        var dropped = transactionManager.inTransactionR(tx -> tableRepository.delete(tx, identifier));

        if (dropped) {
            try {
                var tablePath = getTableLocation(identifier);
                if (fileIO.exists(tablePath)) {
                    fileIO.deleteDirectoryQuietly(tablePath);
                }

                for (var it : externalPaths) {
                    if (fileIO.exists(it)) {
                        fileIO.deleteDirectoryQuietly(it);
                    }
                }
            } catch (IOException e) {
                // TODO: log
            }
        } else {
            // TODO: log skip
        }
    }

    @Override
    protected void createTableImpl(Identifier identifier, Schema schema) {
        var schemaManager = getSchemaManager(identifier);
        try {
            // TODO: lock table (advisory locks)
            schemaManager.createTable(schema);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void renameTableImpl(Identifier fromTable, Identifier toTable) {

    }

    @Override
    protected void alterTableImpl(Identifier identifier, List<SchemaChange> changes) throws TableNotExistException, ColumnAlreadyExistException, ColumnNotExistException {
        // TODO: lock (advisory locks)
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
    public View getView(Identifier identifier) throws ViewNotExistException {
        return super.getView(identifier);
    }

    @Override
    public void dropView(Identifier identifier, boolean ignoreIfNotExists) throws ViewNotExistException {
        super.dropView(identifier, ignoreIfNotExists);
    }

    @Override
    public void createView(Identifier identifier, View view, boolean ignoreIfExists) throws ViewAlreadyExistException, DatabaseNotExistException {
        // TODO: lock (advisory locks)
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
        // TODO: lock (advisory locks)
        super.renameView(fromView, toView, ignoreIfNotExists);
    }

    @Override
    public void alterView(Identifier view, List<ViewChange> viewChanges, boolean ignoreIfNotExists) throws ViewNotExistException, DialectAlreadyExistException, DialectNotExistException {
        // TODO: lock (advisory locks)
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
        // TODO: lock (advisory locks)
        super.alterFunction(identifier, changes, ignoreIfNotExists);
    }

    @Override
    public boolean commitSnapshot(Identifier identifier, @Nullable String tableUuid, Snapshot snapshot, List<PartitionStatistics> statistics) {
        return super.commitSnapshot(identifier, tableUuid, snapshot, statistics);
    }

    @Override
    public void createBranch(Identifier identifier, String branch, @Nullable String fromTag) throws TableNotExistException, BranchAlreadyExistException, TagNotExistException {
        // TODO: lock (advisory locks)
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
        // TODO: lock (advisory locks)
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

    private SchemaManager getSchemaManager(Identifier identifier) {
        return new SchemaManager(fileIO, getTableLocation(identifier));
    }
}
