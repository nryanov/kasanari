package kasanari.catalog.paimon;

import kasanari.catalog.paimon.model.DatabaseRecord;
import kasanari.catalog.paimon.model.FunctionRecord;
import kasanari.catalog.paimon.model.TableRecord;
import kasanari.catalog.paimon.model.ViewRecord;
import kasanari.catalog.paimon.repository.BranchRepository;
import kasanari.catalog.paimon.repository.DatabaseRepository;
import kasanari.catalog.paimon.repository.FunctionRepository;
import kasanari.catalog.paimon.repository.jdbc.KasanariCatalogLock;
import kasanari.catalog.paimon.repository.TableRepository;
import kasanari.catalog.paimon.repository.TagRepository;
import kasanari.catalog.paimon.repository.TransactionManager;
import kasanari.catalog.paimon.repository.ViewRepository;
import org.apache.paimon.CoreOptions;
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
import org.apache.paimon.operation.Lock;
import org.apache.paimon.partition.PartitionStatistics;
import org.apache.paimon.rest.responses.GetTagResponse;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.schema.SchemaChange;
import org.apache.paimon.schema.SchemaManager;
import org.apache.paimon.schema.TableSchema;
import org.apache.paimon.table.Table;
import org.apache.paimon.table.TableSnapshot;
import org.apache.paimon.utils.SnapshotNotExistException;
import org.apache.paimon.view.View;
import org.apache.paimon.view.ViewChange;
import org.apache.paimon.view.ViewImpl;
import org.jdbi.v3.core.Handle;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

public class KasanariPaimonCatalog extends AbstractCatalog {
    private final TransactionManager<Handle> transactionManager;
    // TODO: each repository should also accept catalogKey (or warehouse)?
    // TODO: check that current branch is main in each operation?
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
    public void repairDatabase(String databaseName) {
        // TODO
        super.repairDatabase(databaseName);
    }

    @Override
    protected void createTableImpl(Identifier identifier, Schema schema) {
        var schemaManager = getSchemaManager(identifier);
        var path = getTableLocation(identifier);
        try {
            transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
                    var tableSchema = schemaManager.createTable(schema);
                    var properties = collectTableProperties(tableSchema);
                    tableRepository.create(tx, new TableRecord(identifier, properties));
                    return tableSchema;
                }));
        } catch (Exception e) {
            try {
                fileIO.deleteDirectoryQuietly(path);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            throw new RuntimeException(e);
        }
    }

    @Override
    protected void renameTableImpl(Identifier fromTable, Identifier toTable) {
        var fromPath = getTableLocation(fromTable);
        var toPath = getTableLocation(toTable);
        transactionManager.inTransaction(tx -> tableRepository.rename(tx, fromTable, toTable));
        try {
            fileIO.rename(fromPath, toPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void alterTableImpl(Identifier identifier, List<SchemaChange> changes) throws TableNotExistException, ColumnAlreadyExistException, ColumnNotExistException {
        var schemaManager = getSchemaManager(identifier);
        transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
            var updatedSchema = schemaManager.commitChanges(changes);
            var properties = collectTableProperties(updatedSchema);
            tableRepository.alter(tx, new TableRecord(identifier, properties));
            return updatedSchema;
        }));
    }

    @Override
    public void repairCatalog() {
        // TODO
        super.repairCatalog();
    }

    @Override
    public void repairTable(Identifier identifier) throws TableNotExistException {
        super.repairTable(identifier);
    }

    @Override
    protected TableSchema loadTableSchema(Identifier identifier) throws TableNotExistException {
        var isExists = transactionManager.inTransactionR(tx -> tableRepository.exists(tx, identifier));
        if (!isExists) {
            throw new TableNotExistException(identifier);
        }

        var location = getTableLocation(identifier);
        return tableSchemaInFileSystem(location, identifier.getBranchNameOrDefault())
                .orElseThrow(
                        () -> new RuntimeException("There is no paimon table in " + location));
    }

    @Override
    public Table getTableById(String tableId) throws TableIdNotExistException {
        // TODO: implement?
        return super.getTableById(tableId);
    }

    @Override
    public View getView(Identifier identifier) throws ViewNotExistException {
        var maybe = transactionManager.inTransactionR(tx -> viewRepository.find(tx, identifier));
        if (maybe.isEmpty()) {
            throw new ViewNotExistException(identifier);
        }

        var view = maybe.get();
        var location = getTableLocation(identifier);

        var viewSchema = tableSchemaInFileSystem(location, identifier.getBranchNameOrDefault())
                .orElseThrow(() -> new RuntimeException("There is no paimon view in " + location));

        return new ViewImpl(
                identifier,
                viewSchema.fields(),
                view.query(),
                view.dialects(),
                view.comment().orElse(null),
                view.options()
        );
    }

    @Override
    public void dropView(Identifier identifier, boolean ignoreIfNotExists) throws ViewNotExistException {
        var dropped = transactionManager.inTransactionR(tx -> viewRepository.delete(tx, identifier));

        if (dropped) {
            try {
                var viewPath = getTableLocation(identifier);
                if (fileIO.exists(viewPath)) {
                    fileIO.deleteDirectoryQuietly(viewPath);
                }
            } catch (IOException e) {
                // TODO: log
            }
        } else {
            // TODO: log
            throw new ViewNotExistException(identifier);
        }
    }

    @Override
    public void createView(Identifier identifier, View view, boolean ignoreIfExists) throws ViewAlreadyExistException, DatabaseNotExistException {
        var schemaManager = getSchemaManager(identifier);

        var fields = view.rowType().getFields();
        var viewSchema = new Schema(fields, List.of(), List.of(), view.options(), view.comment().orElse(null));
        transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
            var schema = schemaManager.createTable(viewSchema);
            viewRepository.create(tx, new ViewRecord(identifier, view));
            return schema;
        }));
    }

    @Override
    public List<String> listViews(String databaseName) throws DatabaseNotExistException {
        // todo: validate database existence
        return transactionManager.inTransactionR(tx -> viewRepository.findAll(tx, databaseName))
                .stream().map(ViewRecord::name)
                .toList();
    }

    @Override
    public void renameView(Identifier fromView, Identifier toView, boolean ignoreIfNotExists) throws ViewNotExistException, ViewAlreadyExistException {
        var fromPath = getTableLocation(fromView);
        var toPath = getTableLocation(toView);

        // TODO: check from/to view existence
        var renamed = transactionManager.inTransactionR(tx -> viewRepository.rename(tx, fromView, toView));

        if (renamed) {
            try {
                fileIO.rename(fromPath, toPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (!ignoreIfNotExists) {
                throw new ViewNotExistException(fromView);
            }
        }
    }

    @Override
    public void alterView(Identifier view, List<ViewChange> viewChanges, boolean ignoreIfNotExists) throws ViewNotExistException, DialectAlreadyExistException, DialectNotExistException {
        var maybe = transactionManager.inTransactionR(tx -> viewRepository.find(tx, view));
        if (maybe.isEmpty()) {
            throw new ViewNotExistException(view);
        }

        var current = maybe.get();
        var comment = current.comment();
        var options = new HashMap<>(current.options());
        var dialects = new HashMap<>(current.dialects());
        var query = current.query();

        viewChanges.forEach(change -> {
            switch (change) {
                case ViewChange.RemoveViewOption i -> {}
                case ViewChange.SetViewOption i -> {}
                case ViewChange.UpdateViewComment i -> {}
                case ViewChange.AddDialect i -> {}
                case ViewChange.DropDialect i -> {}
                case ViewChange.UpdateDialect i -> {}
                default -> {}
            }
        });

        transactionManager.inTransaction(tx -> runWithLock(tx, view, () -> {
            viewRepository.alter(tx, new ViewRecord(
                    view.getDatabaseName(),
                    view.getTableName(),
                    query,
                    dialects,
                    options,
                    comment
            ));
            return true;
        }));
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
    public List<String> listFunctions(String databaseName) {
        return transactionManager.inTransactionR(tx -> functionRepository.findAll(tx, databaseName))
                .stream().map(FunctionRecord::name)
                .toList();
    }

    @Override
    public void createFunction(Identifier identifier, Function function, boolean ignoreIfExists) throws FunctionAlreadyExistException, DatabaseNotExistException {
        var database = identifier.getDatabaseName();
        var functionName = identifier.getObjectName();

        var inputParams = function.inputParams().orElse(List.of());
        var inputParamsIdentifier = Identifier.create(database, functionName + "_input");
        var inputParamsSchemaManager = getSchemaManager(inputParamsIdentifier);
        var inputParamsSchema = new Schema(inputParams, List.of(), List.of(), Map.of(), null);

        var returnParams = function.returnParams().orElse(List.of());
        var returnParamsIdentifier = Identifier.create(database, functionName + "_return");
        var returnParamsSchemaManager = getSchemaManager(returnParamsIdentifier);
        var returnParamsSchema = new Schema(returnParams, List.of(), List.of(), Map.of(), null);

        transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
            inputParamsSchemaManager.createTable(inputParamsSchema);
            returnParamsSchemaManager.createTable(returnParamsSchema);

            functionRepository.create(tx, new FunctionRecord(identifier, function));
            return true;
        }));
    }

    @Override
    public Function getFunction(Identifier identifier) throws FunctionNotExistException {
        var maybe = transactionManager.inTransactionR(tx -> functionRepository.find(tx, identifier));
        if (maybe.isEmpty()) {
            throw new FunctionNotExistException(identifier);
        }

        var func = maybe.get();

        // TODO
        return null;
    }

    @Override
    public void dropFunction(Identifier identifier, boolean ignoreIfNotExists) throws FunctionNotExistException {
        var deleted = transactionManager.inTransactionR(tx -> functionRepository.delete(tx, identifier));

        if (!deleted) {
            if (!ignoreIfNotExists) {
                throw new FunctionNotExistException(identifier);
            }
        }
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
    public Optional<TableSnapshot> loadSnapshot(Identifier identifier) {
        return super.loadSnapshot(identifier);
    }

    @Override
    public Optional<Snapshot> loadSnapshot(Identifier identifier, String version) {
        return super.loadSnapshot(identifier, version);
    }

    @Override
    public PagedList<Snapshot> listSnapshotsPaged(Identifier identifier, @Nullable Integer maxResults, @Nullable String pageToken) {
        return super.listSnapshotsPaged(identifier, maxResults, pageToken);
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

    // todo: throw exception instead or runtimeException
    private  <T> T runWithLock(Handle handle, Identifier identifier, Callable<T> callable) {
        try {
            if (!lockEnabled()) {
                return callable.call();
            }

            var lock = new KasanariCatalogLock(handle);
            return Lock.fromCatalog(lock, identifier).runWithLock(callable);
        } catch (Exception e) {
            // todo: log & domain error
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> collectTableProperties(TableSchema tableSchema) {
        var properties = new HashMap<>(tableSchema.options());
        properties.putAll(convertToPropertiesTableKey(tableSchema));
        return properties;
    }

    private Map<String, String> convertToPropertiesTableKey(TableSchema tableSchema) {
        var properties = new HashMap<String, String>();
        if (!tableSchema.primaryKeys().isEmpty()) {
            properties.put(
                    CoreOptions.PRIMARY_KEY.key(), String.join(",", tableSchema.primaryKeys()));
        }
        if (!tableSchema.partitionKeys().isEmpty()) {
            properties.put(
                    CoreOptions.PARTITION.key(), String.join(",", tableSchema.partitionKeys()));
        }
        if (!tableSchema.bucketKeys().isEmpty()) {
            properties.put(
                    CoreOptions.BUCKET_KEY.key(), String.join(",", tableSchema.bucketKeys()));
        }
        return properties;
    }
}
