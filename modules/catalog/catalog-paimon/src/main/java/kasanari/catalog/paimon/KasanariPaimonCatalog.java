package kasanari.catalog.paimon;

import kasanari.catalog.paimon.model.BranchRecord;
import kasanari.catalog.paimon.model.DatabaseRecord;
import kasanari.catalog.paimon.model.FunctionRecord;
import kasanari.catalog.paimon.model.PartitionStateRecord;
import kasanari.catalog.paimon.model.TableRecord;
import kasanari.catalog.paimon.model.TagRecord;
import kasanari.catalog.paimon.model.ViewRecord;
import kasanari.catalog.paimon.repository.BranchRepository;
import kasanari.catalog.paimon.repository.DatabaseRepository;
import kasanari.catalog.paimon.repository.FunctionRepository;
import kasanari.catalog.paimon.repository.PartitionStateRepository;
import kasanari.catalog.paimon.repository.jdbc.JdbcBranchRepository;
import kasanari.catalog.paimon.repository.jdbc.JdbcDatabaseRepository;
import kasanari.catalog.paimon.repository.jdbc.JdbcFunctionRepository;
import kasanari.catalog.paimon.repository.jdbc.JdbcPartitionStateRepository;
import kasanari.catalog.paimon.repository.jdbc.JdbcTableInitializer;
import kasanari.catalog.paimon.repository.jdbc.JdbcTableRepository;
import kasanari.catalog.paimon.repository.jdbc.JdbcTagRepository;
import kasanari.catalog.paimon.repository.jdbc.JdbcViewRepository;
import kasanari.catalog.paimon.repository.jdbc.KasanariCatalogLock;
import kasanari.catalog.paimon.repository.TableRepository;
import kasanari.catalog.paimon.repository.TagRepository;
import kasanari.catalog.paimon.repository.ViewRepository;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import org.apache.paimon.CoreOptions;
import org.apache.paimon.PagedList;
import org.apache.paimon.Snapshot;
import org.apache.paimon.catalog.AbstractCatalog;
import org.apache.paimon.catalog.CatalogUtils;
import org.apache.paimon.catalog.CatalogContext;
import org.apache.paimon.catalog.CatalogLoader;
import org.apache.paimon.catalog.Database;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.catalog.PropertyChange;
import org.apache.paimon.catalog.TableMetadata;
import org.apache.paimon.consumer.Consumer;
import org.apache.paimon.consumer.ConsumerManager;
import org.apache.paimon.consumer.ConsumerInfo;
import org.apache.paimon.fs.FileIO;
import org.apache.paimon.fs.Path;
import org.apache.paimon.function.Function;
import org.apache.paimon.function.FunctionChange;
import org.apache.paimon.function.FunctionDefinition;
import org.apache.paimon.function.FunctionImpl;
import org.apache.paimon.operation.Lock;
import org.apache.paimon.partition.Partition;
import org.apache.paimon.partition.PartitionStatistics;
import org.apache.paimon.rest.responses.GetTagResponse;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.schema.SchemaChange;
import org.apache.paimon.schema.SchemaManager;
import org.apache.paimon.schema.TableSchema;
import org.apache.paimon.table.Instant;
import org.apache.paimon.table.RollbackHelper;
import org.apache.paimon.tag.Tag;
import org.apache.paimon.table.Table;
import org.apache.paimon.table.TableSnapshot;
import org.apache.paimon.utils.ChangelogManager;
import org.apache.paimon.utils.FileSystemBranchManager;
import org.apache.paimon.utils.SnapshotManager;
import org.apache.paimon.utils.SnapshotNotExistException;
import org.apache.paimon.utils.TagManager;
import org.apache.paimon.utils.TimeUtils;
import org.apache.paimon.view.View;
import org.apache.paimon.view.ViewChange;
import org.apache.paimon.view.ViewImpl;
import org.jdbi.v3.core.Handle;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

public class KasanariPaimonCatalog extends AbstractCatalog {
    private final KasanariDataSource dataSource;
    private final TransactionManager<Handle> transactionManager;
    // TODO: check that current branch is main in each operation?
    private final DatabaseRepository<Handle> databaseRepository;
    private final TableRepository<Handle> tableRepository;
    private final ViewRepository<Handle> viewRepository;
    private final FunctionRepository<Handle> functionRepository;
    private final TagRepository<Handle> tagRepository;
    private final BranchRepository<Handle> branchRepository;
    private final PartitionStateRepository<Handle> partitionStateRepository;

    private final FileIO fileIO;
    private final String catalogKey;
    private final String warehouse;

    public KasanariPaimonCatalog(FileIO fileIO, String catalogKey, CatalogContext context, String warehouse) {
        super(fileIO, context);

        var options = context.options().toMap();
        this.dataSource = new KasanariDataSource(options);
        this.transactionManager = new JdbcTransactionManager(dataSource);
        this.databaseRepository = new JdbcDatabaseRepository(catalogKey);
        this.tableRepository = new JdbcTableRepository(catalogKey);
        this.viewRepository = new JdbcViewRepository(catalogKey);
        this.functionRepository = new JdbcFunctionRepository(catalogKey);
        this.tagRepository = new JdbcTagRepository(catalogKey);
        this.branchRepository = new JdbcBranchRepository(catalogKey);
        this.partitionStateRepository = new JdbcPartitionStateRepository(catalogKey);
        new JdbcTableInitializer(dataSource).initialize();

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
                default -> {
                }
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
    public PagedList<String> listTablesPaged(
            String databaseName,
            Integer maxResults,
            String pageToken,
            String tableNamePattern,
            String tableType)
            throws DatabaseNotExistException {
        CatalogUtils.validateNamePattern(this, tableNamePattern);
        CatalogUtils.validateTableType(this, tableType);
        var idAfter = decodePageToken(pageToken);
        var pageSize = defaultPageSize(maxResults);
        var rows = transactionManager.inTransactionR(tx -> tableRepository.findPage(
                tx,
                databaseName,
                toSqlLikePattern(tableNamePattern),
                idAfter,
                pageSize
        ));
        var nextPageToken = rows.size() < pageSize ? null : String.valueOf(rows.getLast().id());
        return new PagedList<>(rows.stream().map(TableRecord::name).toList(), nextPageToken);
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
        CatalogUtils.checkNotSystemDatabase(databaseName);

        try {
            getDatabase(databaseName);
            var tables = listTablesInFileSystem(newDatabasePath(databaseName));
            for (var table : tables) {
                try {
                    repairTable(Identifier.create(databaseName, table));
                } catch (TableNotExistException e) {
                    // ignore
                }
            }
        } catch (DatabaseNotExistException e) {
            createDatabaseImpl(databaseName, Map.of());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public PagedList<Identifier> listTablesPagedGlobally(
            @Nullable String databaseNamePattern,
            @Nullable String tableNamePattern,
            @Nullable Integer maxResults,
            @Nullable String pageToken) {
        CatalogUtils.validateNamePattern(this, databaseNamePattern);
        CatalogUtils.validateNamePattern(this, tableNamePattern);
        var idAfter = decodePageToken(pageToken);
        var pageSize = defaultPageSize(maxResults);
        var rows = transactionManager.inTransactionR(tx -> tableRepository.findPageGlobally(
                tx,
                toSqlLikePattern(databaseNamePattern),
                toSqlLikePattern(tableNamePattern),
                idAfter,
                pageSize
        ));
        var nextPageToken = rows.size() < pageSize ? null : String.valueOf(rows.getLast().id());
        return new PagedList<>(
                rows.stream().map(it -> Identifier.create(it.database(), it.name())).toList(),
                nextPageToken
        );
    }

    @Override
    protected void createTableImpl(Identifier identifier, Schema schema) {
        var schemaManager = getSchemaManager(identifier);
        var path = getTableLocation(identifier);
        try {
            transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
                var tableSchema = schemaManager.createTable(schema);
                var properties = collectTableProperties(tableSchema);
                tableRepository.create(tx, new TableRecord(identifier, properties, Optional.of(UUID.randomUUID().toString())));
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
        try {
            var databases = listDatabasesInFileSystem(new Path(warehouse()));

            for (var database : databases) {
                repairDatabase(database);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void repairTable(Identifier identifier) throws TableNotExistException {
        CatalogUtils.checkNotBranch(identifier, "repairTable");
        CatalogUtils.checkNotSystemTable(identifier, "repairTable");

        var location = getTableLocation(identifier);
        var tableSchema = tableSchemaInFileSystem(location, Identifier.DEFAULT_MAIN_BRANCH)
                .orElseThrow(() -> new TableNotExistException(identifier));

        var properties = collectTableProperties(tableSchema);
        try {
            transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
                var existing = tableRepository.find(tx, identifier);
                if (existing.isPresent()) {
                    tableRepository.alter(tx, new TableRecord(identifier, properties, existing.get().tableUuid()));
                } else {
                    tableRepository.create(tx, new TableRecord(identifier, properties, Optional.of(UUID.randomUUID().toString())));
                }
                return true;
            }));
        } catch (RuntimeException e) {
            var cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof TableNotExistException tableNotExistException) {
                throw tableNotExistException;
            }
            throw e;
        }
    }

    @Override
    protected TableSchema loadTableSchema(Identifier identifier) throws TableNotExistException {
        var isExists = transactionManager.inTransactionR(tx -> tableRepository.exists(tx, identifier));
        if (!isExists) {
            throw new TableNotExistException(identifier);
        }

        var location = getTableLocation(identifier);
        return tableSchemaInFileSystem(location, identifier.getBranchNameOrDefault())
                .orElseThrow(() -> new RuntimeException("There is no paimon table in " + location));
    }

    @Override
    protected TableMetadata loadTableMetadata(Identifier identifier) throws TableNotExistException {
        var schema = loadTableSchema(identifier);
        var tableRecord = transactionManager.inTransactionR(tx -> tableRepository.find(tx, identifier))
                .orElseThrow(() -> new TableNotExistException(identifier));
        return new TableMetadata(schema, false, tableRecord.tableUuid().orElse(null));
    }

    @Override
    public Table getTableById(String tableId) throws TableIdNotExistException {
        var maybeRecord = transactionManager.inTransactionR(tx -> tableRepository.findByUuid(tx, tableId));
        if (maybeRecord.isPresent()) {
            var record = maybeRecord.get();
            try {
                return getTable(Identifier.create(record.database(), record.name()));
            } catch (TableNotExistException e) {
                throw new TableIdNotExistException(tableId, e);
            }
        }

        try {
            return getTable(Identifier.fromString(tableId));
        } catch (Exception err) {
            throw new TableIdNotExistException(tableId, err);
        }
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
        var comment = new AtomicReference<>(current.comment());
        var options = new HashMap<>(current.options());
        var dialects = new HashMap<>(current.dialects());
        var query = current.query();

        for (var change : viewChanges) {
            switch (change) {
                case ViewChange.RemoveViewOption i -> options.remove(i.key());
                case ViewChange.SetViewOption i -> options.put(i.key(), i.value());
                case ViewChange.UpdateViewComment i -> comment.set(Optional.ofNullable(i.comment()));
                case ViewChange.AddDialect i -> {
                    var currentDialect = dialects.get(i.dialect());
                    if (currentDialect != null) {
                        throw new DialectAlreadyExistException(view, i.dialect());
                    }

                    dialects.put(i.dialect(), i.query());
                }
                case ViewChange.DropDialect i -> {
                    var currentDialect = dialects.remove(i.dialect());
                    if (currentDialect == null) {
                        throw new DialectNotExistException(view, i.dialect());
                    }
                }
                case ViewChange.UpdateDialect i -> {
                    var currentDialect = dialects.get(i.dialect());
                    if (currentDialect == null) {
                        throw new DialectNotExistException(view, i.dialect());
                    }

                    dialects.put(i.dialect(), i.query());
                }
                default -> {
                }
            }
        }

        transactionManager.inTransaction(tx -> runWithLock(tx, view, () -> {
            viewRepository.alter(tx, new ViewRecord(
                    view.getDatabaseName(),
                    view.getTableName(),
                    query,
                    dialects,
                    options,
                    comment.get()
            ));
            return true;
        }));
    }

    @Override
    public PagedList<String> listViewsPaged(
            String databaseName,
            @Nullable Integer maxResults,
            @Nullable String pageToken,
            @Nullable String viewNamePattern)
            throws DatabaseNotExistException {
        CatalogUtils.validateNamePattern(this, viewNamePattern);
        var idAfter = decodePageToken(pageToken);
        var pageSize = defaultPageSize(maxResults);
        var rows = transactionManager.inTransactionR(tx -> viewRepository.findPage(
                tx,
                databaseName,
                toSqlLikePattern(viewNamePattern),
                idAfter,
                pageSize
        ));
        var nextPageToken = rows.size() < pageSize ? null : String.valueOf(rows.getLast().id());
        return new PagedList<>(rows.stream().map(ViewRecord::name).toList(), nextPageToken);
    }

    @Override
    public PagedList<View> listViewDetailsPaged(
            String databaseName,
            @Nullable Integer maxResults,
            @Nullable String pageToken,
            @Nullable String viewNamePattern)
            throws DatabaseNotExistException {
        CatalogUtils.validateNamePattern(this, viewNamePattern);

        var pagedNames = listViewsPaged(databaseName, maxResults, pageToken, viewNamePattern);
        var details = pagedNames.getElements().stream()
                .map(
                        name -> {
                            try {
                                return getView(Identifier.create(databaseName, name));
                            } catch (ViewNotExistException ignored) {
                                return null;
                            }
                        })
                .filter(Objects::nonNull)
                .toList();

        return new PagedList<>(details, pagedNames.getNextPageToken());
    }

    @Override
    public PagedList<Identifier> listViewsPagedGlobally(
            @Nullable String databaseNamePattern,
            @Nullable String viewNamePattern,
            @Nullable Integer maxResults,
            @Nullable String pageToken) {
        CatalogUtils.validateNamePattern(this, databaseNamePattern);
        CatalogUtils.validateNamePattern(this, viewNamePattern);
        var idAfter = decodePageToken(pageToken);
        var pageSize = defaultPageSize(maxResults);
        var rows = transactionManager.inTransactionR(tx -> viewRepository.findPageGlobally(
                tx,
                toSqlLikePattern(databaseNamePattern),
                toSqlLikePattern(viewNamePattern),
                idAfter,
                pageSize
        ));
        var nextPageToken = rows.size() < pageSize ? null : String.valueOf(rows.getLast().id());
        return new PagedList<>(
                rows.stream().map(it -> Identifier.create(it.database(), it.name())).toList(),
                nextPageToken
        );
    }

    @Override
    public List<String> listFunctions(String databaseName) {
        return transactionManager.inTransactionR(tx -> functionRepository.findAll(tx, databaseName))
                .stream().map(FunctionRecord::name)
                .toList();
    }

    @Override
    public PagedList<String> listFunctionsPaged(
            String databaseName,
            @Nullable Integer maxResults,
            @Nullable String pageToken,
            @Nullable String functionNamePattern)
            throws DatabaseNotExistException {
        CatalogUtils.validateNamePattern(this, functionNamePattern);
        var idAfter = decodePageToken(pageToken);
        var pageSize = defaultPageSize(maxResults);
        var rows = transactionManager.inTransactionR(tx -> functionRepository.findPage(
                tx,
                databaseName,
                toSqlLikePattern(functionNamePattern),
                idAfter,
                pageSize
        ));
        var nextPageToken = rows.size() < pageSize ? null : String.valueOf(rows.getLast().id());
        return new PagedList<>(rows.stream().map(FunctionRecord::name).toList(), nextPageToken);
    }

    @Override
    public PagedList<Identifier> listFunctionsPagedGlobally(
            @Nullable String databaseNamePattern,
            @Nullable String functionNamePattern,
            @Nullable Integer maxResults,
            @Nullable String pageToken) {
        CatalogUtils.validateNamePattern(this, databaseNamePattern);
        CatalogUtils.validateNamePattern(this, functionNamePattern);
        var idAfter = decodePageToken(pageToken);
        var pageSize = defaultPageSize(maxResults);
        var rows = transactionManager.inTransactionR(tx -> functionRepository.findPageGlobally(
                tx,
                toSqlLikePattern(databaseNamePattern),
                toSqlLikePattern(functionNamePattern),
                idAfter,
                pageSize
        ));
        var nextPageToken = rows.size() < pageSize ? null : String.valueOf(rows.getLast().id());
        return new PagedList<>(
                rows.stream().map(it -> Identifier.create(it.database(), it.name())).toList(),
                nextPageToken
        );
    }

    @Override
    public PagedList<Function> listFunctionDetailsPaged(
            String databaseName,
            @Nullable Integer maxResults,
            @Nullable String pageToken,
            @Nullable String functionNamePattern)
            throws DatabaseNotExistException {
        CatalogUtils.validateNamePattern(this, functionNamePattern);
        var pagedNames = listFunctionsPaged(databaseName, maxResults, pageToken, functionNamePattern);
        var details = pagedNames.getElements().stream()
                .map(
                        name -> {
                            try {
                                return getFunction(Identifier.create(databaseName, name));
                            } catch (FunctionNotExistException ignored) {
                                return null;
                            }
                        })
                .filter(Objects::nonNull)
                .toList();
        return new PagedList<>(details, pagedNames.getNextPageToken());
    }

    @Override
    public void rollbackTo(Identifier identifier, Instant instant, @Nullable Long fromSnapshot) throws TableNotExistException {
        try {
            transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
                ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());
                var snapshotManager = snapshotManager(identifier);
                var latestId = snapshotManager.latestSnapshotId();

                if (fromSnapshot != null && !fromSnapshot.equals(latestId)) {
                    throw new IllegalArgumentException(String.format(
                            "Rollback requires current latest snapshot to be %s but was %s.",
                            fromSnapshot,
                            latestId)
                    );
                }

                Snapshot retained;

                switch (instant) {
                    case Instant.SnapshotInstant i -> {
                        var id = i.getSnapshotId();
                        if (!snapshotManager.snapshotExists(id)) {
                            throw new IllegalArgumentException(String.format("Rollback snapshot '%s' doesn't exist.", id));
                        }
                        retained = snapshotManager.snapshot(id);
                    }
                    case Instant.TagInstant i ->
                            retained = tagManager(identifier).getOrThrow(i.getTagName()).trimToSnapshot();
                    default -> throw new IllegalArgumentException("Unsupported rollback instant: " + instant);
                }

                var branch = identifier.getBranchNameOrDefault();
                var tablePath = getTableLocation(identifier);
                var rollbackHelper = new RollbackHelper(
                        snapshotManager,
                        new ChangelogManager(fileIO, tablePath, branch),
                        tagManager(identifier),
                        fileIO
                );

                rollbackHelper.cleanLargerThan(retained);
                if (instant instanceof Instant.TagInstant) {
                    rollbackHelper.createSnapshotFileIfNeeded(retained);
                }

                return true;
            }));
        } catch (RuntimeException e) {
            var cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof TableNotExistException tableNotExistException) {
                throw tableNotExistException;
            }
            throw e;
        }
    }

    @Override
    public void rollbackSchema(Identifier identifier, long schemaId) throws TableNotExistException {
        try {
            transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
                ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());
                var branch = identifier.getBranchNameOrDefault();
                var tablePath = getTableLocation(identifier);
                var schemaManager = new SchemaManager(fileIO, tablePath, branch);
                try {
                    schemaManager.rollbackTo(
                            schemaId,
                            snapshotManager(identifier),
                            tagManager(identifier),
                            new ChangelogManager(fileIO, tablePath, branch)
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }));
        } catch (RuntimeException e) {
            var cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof TableNotExistException tableNotExistException) {
                throw tableNotExistException;
            }
            throw e;
        }
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

        var database = identifier.getDatabaseName();
        var functionName = identifier.getObjectName();

        var inputParamsIdentifier = Identifier.create(database, functionName + "_input");
        var inputParamsLocation = getTableLocation(inputParamsIdentifier);
        var inputParamsSchema = tableSchemaInFileSystem(inputParamsLocation, inputParamsIdentifier.getBranchNameOrDefault())
                .orElseThrow(() -> new RuntimeException("There is no paimon function in " + inputParamsLocation));

        var returnParamsIdentifier = Identifier.create(database, functionName + "_return");
        var returnParamsLocation = getTableLocation(returnParamsIdentifier);
        var returnParamsSchema = tableSchemaInFileSystem(returnParamsLocation, returnParamsIdentifier.getBranchNameOrDefault())
                .orElseThrow(() -> new RuntimeException("There is no paimon function in " + returnParamsLocation));

        var funcDefinitions = new HashMap<String, FunctionDefinition>();
        func.definitions().forEach((name, def) -> funcDefinitions.put(name, def.toPaimon()));

        return new FunctionImpl(
                identifier,
                inputParamsSchema.fields(),
                returnParamsSchema.fields(),
                func.deterministic(),
                funcDefinitions,
                func.comment().orElse(null),
                func.options()
        );
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
        var maybe = transactionManager.inTransactionR(tx -> functionRepository.find(tx, identifier));
        if (maybe.isEmpty()) {
            if (!ignoreIfNotExists) {
                throw new FunctionNotExistException(identifier);
            } else {
                return;
            }
        }

        var func = maybe.get();

        var definitions = new HashMap<>(func.definitions());
        var options = new HashMap<>(func.options());
        var comment = new AtomicReference<>(func.comment());

        for (var change : changes) {
            switch (change) {
                case FunctionChange.RemoveFunctionOption c -> options.remove(c.key());
                case FunctionChange.SetFunctionOption c -> options.put(c.key(), c.value());
                case FunctionChange.UpdateFunctionComment c -> comment.set(Optional.ofNullable(c.comment()));
                case FunctionChange.AddDefinition c -> {
                    var def = definitions.get(c.name());
                    if (def != null) {
                        throw new DefinitionAlreadyExistException(identifier, c.name());
                    }

                    definitions.put(c.name(), FunctionRecord.fromPaimon(c.definition()));
                }
                case FunctionChange.DropDefinition c -> {
                    var def = definitions.remove(c.name());
                    if (def == null) {
                        throw new DefinitionNotExistException(identifier, c.name());
                    }
                }
                case FunctionChange.UpdateDefinition c -> {
                    var def = definitions.get(c.name());
                    if (def == null) {
                        throw new DefinitionNotExistException(identifier, c.name());
                    }

                    definitions.put(c.name(), FunctionRecord.fromPaimon(c.definition()));
                }
                default -> {
                }
            }
        }

        transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
            functionRepository.alter(tx, new FunctionRecord(
                    func.database(),
                    func.name(),
                    func.deterministic(),
                    definitions,
                    comment.get(),
                    options
            ));
            return true;
        }));
    }

    @Override
    public boolean commitSnapshot(
            Identifier identifier,
            @Nullable String tableUuid,
            Snapshot snapshot,
            List<PartitionStatistics> statistics) {
        try {
            return transactionManager.inTransactionR(tx -> runWithLock(tx, identifier, () -> {
                var tableRecord = tableRepository.find(tx, identifier)
                        .orElseThrow(() -> new RuntimeException(new TableNotExistException(identifier)));

                try {
                    ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());
                } catch (TableNotExistException e) {
                    throw new RuntimeException(e);
                }

                if (tableUuid != null && tableRecord.tableUuid().isPresent() && !tableUuid.equals(tableRecord.tableUuid().get())) {
                    throw new IllegalArgumentException(
                            String.format(
                                    "Table id mismatch for %s. Expected '%s' but got '%s'.",
                                    identifier.getFullName(),
                                    tableRecord.tableUuid().get(),
                                    tableUuid));
                }

                var snapshotManager = snapshotManager(identifier);
                var newSnapshotPath = snapshotManager.snapshotPath(snapshot.id());

                try {
                    if (fileIO.exists(newSnapshotPath)) {
                        return false;
                    }

                    var committed = fileIO.tryToWriteAtomic(newSnapshotPath, snapshot.toJson());

                    if (committed) {
                        snapshotManager.commitLatestHint(snapshot.id());
                        partitionStateRepository.persistCommitStatistics(
                                tx,
                                identifier,
                                identifier.getBranchNameOrDefault(),
                                snapshot.id(),
                                statistics
                        );
                    }

                    return committed;
                } catch (IOException e) {
                    throw new RuntimeException(
                            String.format("Failed to commit snapshot %s for table %s.", snapshot.id(), identifier.getFullName()),
                            e);
                }
            }));
        } catch (RuntimeException e) {
            var cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof TableNotExistException tableNotExistException) {
                throw new RuntimeException(tableNotExistException);
            }
            throw e;
        }
    }

    @Override
    public void createBranch(Identifier identifier, String branch, @Nullable String fromTag)
            throws TableNotExistException, BranchAlreadyExistException, TagNotExistException {
        try {
            transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
                try {
                    ensureTableExistsInFileSystem(identifier, Identifier.DEFAULT_MAIN_BRANCH);
                } catch (TableNotExistException e) {
                    throw new RuntimeException(e);
                }

                if (fromTag == null) {
                    branchManager(identifier).createBranch(branch);
                } else {
                    branchManager(identifier).createBranch(branch, fromTag);
                }

                var branchRecord = new BranchRecord(identifier.getDatabaseName(), identifier.getTableName(), branch, Optional.ofNullable(fromTag));
                branchRepository.create(tx, branchRecord);
                return true;
            }));
        } catch (RuntimeException e) {
            var cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof TableNotExistException tne) {
                throw tne;
            }
            var message = Optional.ofNullable(cause.getMessage()).orElse("");
            if (message.contains("already exists")) {
                throw new BranchAlreadyExistException(identifier, branch, e);
            }
            if (fromTag != null && message.contains("doesn't exist")) {
                throw new TagNotExistException(identifier, fromTag, e);
            }
            throw e;
        }
    }

    @Override
    public void dropBranch(Identifier identifier, String branch) throws BranchNotExistException {
        try {
            transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
                branchManager(identifier).dropBranch(branch);
                branchRepository.delete(tx, identifier, branch);
                return true;
            }));
        } catch (RuntimeException e) {
            var message = Optional.ofNullable(e.getMessage()).orElse("");
            if (message.contains("doesn't exist")) {
                throw new BranchNotExistException(identifier, branch, e);
            }
            throw e;
        }
    }

    @Override
    public void renameBranch(Identifier identifier, String fromBranch, String toBranch)
            throws BranchNotExistException, BranchAlreadyExistException {
        try {
            transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
                branchManager(identifier).renameBranch(fromBranch, toBranch);
                branchRepository.rename(tx, identifier, fromBranch, toBranch);
                return true;
            }));
        } catch (RuntimeException e) {
            var message = Optional.ofNullable(e.getMessage()).orElse("");
            if (message.contains("already exists")) {
                throw new BranchAlreadyExistException(identifier, toBranch, e);
            }
            if (message.contains("doesn't exist")) {
                throw new BranchNotExistException(identifier, fromBranch, e);
            }
            throw e;
        }
    }

    @Override
    public void fastForward(Identifier identifier, String branch) throws BranchNotExistException {
        try {
            transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
                branchManager(identifier).fastForward(branch);
                branchRepository.fastForward(tx, identifier, branch);
                return true;
            }));
        } catch (RuntimeException e) {
            var message = Optional.ofNullable(e.getMessage()).orElse("");
            if (message.contains("doesn't exist")) {
                throw new BranchNotExistException(identifier, branch, e);
            }
            throw e;
        }
    }

    @Override
    public List<String> listBranches(Identifier identifier) throws TableNotExistException {
        ensureTableExistsInFileSystem(identifier, Identifier.DEFAULT_MAIN_BRANCH);

        var branches = new HashSet<String>();
        branches.add(Identifier.DEFAULT_MAIN_BRANCH);
        var existingBranches = transactionManager.inTransactionR(tx -> branchRepository.findAll(tx, identifier))
                .stream().map(BranchRecord::branchName).toList();
        branches.addAll(existingBranches);

        return branches.stream().sorted().toList();
    }

    @Override
    public GetTagResponse getTag(Identifier identifier, String tagName)
            throws TableNotExistException, TagNotExistException {
        ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());

        var tag = tagManager(identifier).get(tagName);
        if (tag.isPresent()) {
            return toGetTagResponse(tagName, tag.get());
        }

        var metadata = transactionManager.inTransactionR(tx -> tagRepository.find(tx, identifier, tagName));
        if (metadata.isPresent()) {
            var meta = metadata.get();
            try {
                var snapshot = snapshotManager(identifier).snapshot(meta.snapshotId());
                return new GetTagResponse(
                        tagName,
                        snapshot,
                        meta.tagCreateTime().orElse(null),
                        meta.tagTimeRetained().orElse(null)
                );
            } catch (RuntimeException ignored) {
                // Fall through to TagNotExistException if metadata points to a missing snapshot.
            }
        }

        throw new TagNotExistException(identifier, tagName);
    }

    @Override
    public void createTag(
            Identifier identifier,
            String tagName,
            @Nullable Long snapshotId,
            @Nullable String timeRetained,
            boolean ignoreIfExists)
            throws TableNotExistException, SnapshotNotExistException, TagAlreadyExistException {
        try {
            transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
                try {
                    ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());
                } catch (TableNotExistException e) {
                    throw new RuntimeException(e);
                }

                var snapshotManager = snapshotManager(identifier);
                var targetSnapshotId = snapshotId == null ? snapshotManager.latestSnapshotId() : snapshotId;
                if (targetSnapshotId == null || !snapshotManager.snapshotExists(targetSnapshotId)) {
                    if (snapshotId == null) {
                        throw new RuntimeException(new SnapshotNotExistException("Snapshot does not exist for table " + identifier.getFullName()));
                    }
                    throw new RuntimeException(new SnapshotNotExistException(snapshotId));
                }

                var snapshot = snapshotManager.snapshot(targetSnapshotId);
                Duration retainedDuration = timeRetained == null ? null : TimeUtils.parseDuration(timeRetained);

                tagManager(identifier).createTag(snapshot, tagName, retainedDuration, List.of(), ignoreIfExists);

                var tagRecord = new TagRecord(
                        identifier.getDatabaseName(),
                        identifier.getTableName(),
                        tagName,
                        snapshot.id(),
                        Optional.of(System.currentTimeMillis()),
                        Optional.ofNullable(timeRetained)
                );
                tagRepository.create(tx, tagRecord, ignoreIfExists);

                return true;
            }));
        } catch (RuntimeException e) {
            var cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof TableNotExistException tne) {
                throw tne;
            }
            if (cause instanceof SnapshotNotExistException sne) {
                throw sne;
            }
            var message = Optional.ofNullable(cause.getMessage()).orElse("");
            if (message.contains("already exists")) {
                throw new TagAlreadyExistException(identifier, tagName, e);
            }
            throw e;
        }
    }

    @Override
    public PagedList<String> listTagsPaged(
            Identifier identifier,
            @Nullable Integer maxResults,
            @Nullable String pageToken,
            @Nullable String tagNamePrefix)
            throws TableNotExistException {
        ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());
        var idAfter = decodePageToken(pageToken);
        var pageSize = defaultPageSize(maxResults);
        var rows = transactionManager.inTransactionR(tx -> tagRepository.findPage(
                tx,
                identifier,
                toSqlLikePattern(tagNamePrefix),
                idAfter,
                pageSize
        ));

        var nextPageToken = rows.size() < pageSize ? null : String.valueOf(rows.getLast().id());
        return new PagedList<>(rows.stream().map(TagRecord::tagName).toList(), nextPageToken);
    }

    @Override
    public void deleteTag(Identifier identifier, String tagName) throws TableNotExistException, TagNotExistException {
        ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());

        var deleted = transactionManager.inTransactionR(tx -> runWithLock(tx, identifier, () -> {
            var tagManager = tagManager(identifier);
            var existsInFileSystem = tagManager.tagExists(tagName);
            var existsInMetadata = tagRepository.exists(tx, identifier, tagName);
            if (!existsInFileSystem && !existsInMetadata) {
                return false;
            }

            if (existsInFileSystem) {
                fileIO.deleteQuietly(tagManager.tagPath(tagName));
            }
            if (existsInMetadata) {
                tagRepository.delete(tx, identifier, tagName);
            }
            return true;
        }));

        if (!deleted) {
            throw new TagNotExistException(identifier, tagName);
        }
    }

    @Override
    public Optional<TableSnapshot> loadSnapshot(Identifier identifier) {
        try {
            ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());
        } catch (TableNotExistException e) {
            throw new RuntimeException(e);
        }

        var snapshot = snapshotManager(identifier).latestSnapshot();
        if (snapshot == null) {
            return Optional.empty();
        }

        // TODO: fill-in fields
        return Optional.of(new TableSnapshot(snapshot, 0L, 0L, 0L, snapshot.timeMillis()));
    }

    @Override
    public Optional<Snapshot> loadSnapshot(Identifier identifier, String version) {
        try {
            ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());
        } catch (TableNotExistException e) {
            throw new RuntimeException(e);
        }

        var snapshotManager = snapshotManager(identifier);
        if ("LATEST".equalsIgnoreCase(version)) {
            return Optional.ofNullable(snapshotManager.latestSnapshot());
        }
        if ("EARLIEST".equalsIgnoreCase(version)) {
            var earliestId = snapshotManager.earliestSnapshotId();
            return earliestId == null ? Optional.empty() : Optional.of(snapshotManager.snapshot(earliestId));
        }

        try {
            long snapshotId = Long.parseLong(version);
            return snapshotManager.snapshotExists(snapshotId)
                    ? Optional.of(snapshotManager.snapshot(snapshotId))
                    : Optional.empty();
        } catch (NumberFormatException ignored) {
            return tagManager(identifier).get(version).map(Tag::trimToSnapshot);
        }
    }

    @Override
    public PagedList<Snapshot> listSnapshotsPaged(Identifier identifier, @Nullable Integer maxResults, @Nullable String pageToken) {
        try {
            ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());
        } catch (TableNotExistException e) {
            throw new RuntimeException(e);
        }

        List<Snapshot> snapshots;
        try {
            snapshots = snapshotManager(identifier).safelyGetAllSnapshots().stream()
                    .sorted(Comparator.comparingLong(Snapshot::id))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var start = decodePageToken(pageToken);
        if (start >= snapshots.size()) {
            return new PagedList<>(List.of(), null);
        }

        var pageSize = maxResults == null || maxResults <= 0 ? snapshots.size() : maxResults;
        var end = Math.min(snapshots.size(), start + pageSize);
        String nextPageToken = end < snapshots.size() ? String.valueOf(end) : null;
        return new PagedList<>(snapshots.subList(start, end), nextPageToken);
    }

    @Override
    public CatalogLoader catalogLoader() {
        return new KasanariCatalogLoader(fileIO, catalogKey, context, warehouse);
    }

    @Override
    public boolean caseSensitive() {
        return true;
    }

    @Override
    public boolean supportsListObjectsPaged() {
        return true;
    }

    @Override
    public boolean supportsListByPattern() {
        return true;
    }

    @Override
    public boolean supportsVersionManagement() {
        return true;
    }

    @Override
    public void close() throws Exception {
        dataSource.close();
    }

    @Override
    public PagedList<ConsumerInfo> listConsumersPaged(Identifier identifier, @Nullable Integer maxResults, @Nullable String pageToken) throws TableNotExistException {
        ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());

        var manager = consumerManager(identifier);
        var consumers = manager.listAllIds().stream()
                .sorted()
                .map(consumerId -> manager.consumer(consumerId)
                        .map(consumer -> new ConsumerInfo(consumerId, consumer.nextSnapshot()))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        var start = decodePageToken(pageToken);

        if (start >= consumers.size()) {
            return new PagedList<>(List.of(), null);
        }

        var pageSize = maxResults == null || maxResults <= 0 ? consumers.size() : maxResults;
        var end = Math.min(consumers.size(), start + pageSize);
        String nextPageToken = end < consumers.size() ? String.valueOf(end) : null;
        return new PagedList<>(consumers.subList(start, end), nextPageToken);
    }

    @Override
    public void resetConsumer(Identifier identifier, String consumerId, @Nullable Long nextSnapshotId) throws TableNotExistException {
        try {
            transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
                try {
                    ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());
                } catch (TableNotExistException e) {
                    throw new RuntimeException(e);
                }

                var consumerManager = consumerManager(identifier);
                if (nextSnapshotId == null) {
                    consumerManager.deleteConsumer(consumerId);
                    return true;
                }

                consumerManager.resetConsumer(consumerId, new Consumer(nextSnapshotId));
                return true;
            }));
        } catch (RuntimeException e) {
            var cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof TableNotExistException tableNotExistException) {
                throw tableNotExistException;
            }
            throw e;
        }
    }

    @Override
    public void markDonePartitions(Identifier identifier, List<Map<String, String>> partitions) throws TableNotExistException {
        try {
            transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
                try {
                    ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());
                } catch (TableNotExistException e) {
                    throw new RuntimeException(e);
                }
                partitionStateRepository.markDone(tx, identifier, identifier.getBranchNameOrDefault(), partitions);
                return true;
            }));
        } catch (RuntimeException e) {
            var cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof TableNotExistException tableNotExistException) {
                throw tableNotExistException;
            }
            throw e;
        }
    }

    @Override
    public List<Partition> listPartitions(Identifier identifier) throws TableNotExistException {
        ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());
        return transactionManager.inTransactionR(tx -> partitionStateRepository.findAll(tx, identifier, identifier.getBranchNameOrDefault()))
                .stream()
                .map(PartitionStateRecord::toPartition)
                .toList();
    }

    @Override
    public PagedList<Partition> listPartitionsPaged(Identifier identifier, Integer maxResults, String pageToken, String partitionNamePattern) throws TableNotExistException {
        if (partitionNamePattern != null && !partitionNamePattern.isBlank()) {
            throw new UnsupportedOperationException("Current catalog does not support partition name pattern filter.");
        }
        ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());
        var idAfter = decodePageToken(pageToken);
        var pageSize = defaultPageSize(maxResults);
        var rows = transactionManager.inTransactionR(tx -> partitionStateRepository.findPage(
                tx,
                identifier,
                identifier.getBranchNameOrDefault(),
                idAfter,
                pageSize
        ));
        var nextPageToken = rows.size() < pageSize
                ? null
                : String.valueOf(rows.get(rows.size() - 1).id());
        return new PagedList<>(rows.stream().map(PartitionStateRecord::toPartition).toList(), nextPageToken);
    }

    @Override
    public List<Partition> listPartitionsByNames(Identifier identifier, List<Map<String, String>> partitions) throws TableNotExistException {
        if (partitions.size() > 1000) {
            throw new IllegalArgumentException(
                    String.format("The number of partition specs to list should not exceed 1000, but is %d", partitions.size()));
        }
        if (partitions.isEmpty()) {
            return List.of();
        }

        ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());
        return transactionManager.inTransactionR(tx -> partitionStateRepository.findBySpecs(
                        tx,
                        identifier,
                        identifier.getBranchNameOrDefault(),
                        partitions
                ))
                .stream()
                .map(PartitionStateRecord::toPartition)
                .toList();
    }

    @Override
    public void createPartitions(Identifier identifier, List<Map<String, String>> partitions) throws TableNotExistException {
        try {
            transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
                try {
                    ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());
                } catch (TableNotExistException e) {
                    throw new RuntimeException(e);
                }
                partitionStateRepository.createPartitions(tx, identifier, identifier.getBranchNameOrDefault(), partitions);
                return true;
            }));
        } catch (RuntimeException e) {
            var cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof TableNotExistException tableNotExistException) {
                throw tableNotExistException;
            }
            throw e;
        }
    }

    @Override
    public void dropPartitions(Identifier identifier, List<Map<String, String>> partitions) throws TableNotExistException {
        try {
            transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
                try {
                    ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());
                } catch (TableNotExistException e) {
                    throw new RuntimeException(e);
                }
                partitionStateRepository.dropPartitions(tx, identifier, identifier.getBranchNameOrDefault(), partitions);
                return true;
            }));
        } catch (RuntimeException e) {
            var cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof TableNotExistException tableNotExistException) {
                throw tableNotExistException;
            }
            throw e;
        }
    }

    @Override
    public void alterPartitions(Identifier identifier, List<PartitionStatistics> partitions) throws TableNotExistException {
        try {
            transactionManager.inTransaction(tx -> runWithLock(tx, identifier, () -> {
                try {
                    ensureTableExistsInFileSystem(identifier, identifier.getBranchNameOrDefault());
                } catch (TableNotExistException e) {
                    throw new RuntimeException(e);
                }
                partitionStateRepository.alterPartitions(tx, identifier, identifier.getBranchNameOrDefault(), partitions);
                return true;
            }));
        } catch (RuntimeException e) {
            var cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof TableNotExistException tableNotExistException) {
                throw tableNotExistException;
            }
            throw e;
        }
    }

    @Override
    public boolean supportsPartitionModification() {
        return true;
    }

    private SchemaManager getSchemaManager(Identifier identifier) {
        return new SchemaManager(fileIO, getTableLocation(identifier));
    }

    // todo: throw exception instead or runtimeException
    private <T> T runWithLock(Handle handle, Identifier identifier, Callable<T> callable) {
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
            properties.put(CoreOptions.PRIMARY_KEY.key(), String.join(",", tableSchema.primaryKeys()));
        }
        if (!tableSchema.partitionKeys().isEmpty()) {
            properties.put(CoreOptions.PARTITION.key(), String.join(",", tableSchema.partitionKeys()));
        }
        if (!tableSchema.bucketKeys().isEmpty()) {
            properties.put(CoreOptions.BUCKET_KEY.key(), String.join(",", tableSchema.bucketKeys()));
        }
        return properties;
    }

    private void ensureTableExistsInFileSystem(Identifier identifier, String branch)
            throws TableNotExistException {
        var tablePath = getTableLocation(identifier);
        if (tableSchemaInFileSystem(tablePath, branch).isEmpty()) {
            throw new TableNotExistException(identifier);
        }
    }

    private SnapshotManager snapshotManager(Identifier identifier) {
        return new SnapshotManager(
                fileIO,
                getTableLocation(identifier),
                identifier.getBranchNameOrDefault(),
                null,
                null
        );
    }

    private ConsumerManager consumerManager(Identifier identifier) {
        return new ConsumerManager(fileIO, getTableLocation(identifier), identifier.getBranchNameOrDefault());
    }

    private TagManager tagManager(Identifier identifier) {
        return new TagManager(fileIO, getTableLocation(identifier), identifier.getBranchNameOrDefault());
    }

    private FileSystemBranchManager branchManager(Identifier identifier) {
        var tablePath = getTableLocation(identifier);
        var currentBranch = identifier.getBranchNameOrDefault();
        return new FileSystemBranchManager(
                fileIO,
                tablePath,
                new SnapshotManager(fileIO, tablePath, currentBranch, null, null),
                new TagManager(fileIO, tablePath, currentBranch),
                new SchemaManager(fileIO, tablePath, currentBranch));
    }

    private GetTagResponse toGetTagResponse(String tagName, Tag tag) {
        var tagCreateTime =
                tag.getTagCreateTime() == null ? null : tag.getTagCreateTime()
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli();
        var tagTimeRetained = tag.getTagTimeRetained() == null ? null : TimeUtils.formatWithHighestUnit(tag.getTagTimeRetained());

        return new GetTagResponse(tagName, tag.trimToSnapshot(), tagCreateTime, tagTimeRetained);
    }

    private static String toSqlLikePattern(@Nullable String sqlLikePattern) {
        if (sqlLikePattern == null || sqlLikePattern.isEmpty()) {
            return "%";
        }
        var pct = sqlLikePattern.indexOf('%');
        var prefix = pct < 0 ? sqlLikePattern : sqlLikePattern.substring(0, pct);
        if (prefix.isEmpty()) {
            return "%";
        }
        return prefix + "%";
    }

    private static int defaultPageSize(@Nullable Integer maxResults) {
        return maxResults == null || maxResults <= 0 ? 50 : maxResults;
    }

    private static int decodePageToken(@Nullable String pageToken) {
        if (pageToken == null || pageToken.isBlank()) {
            return 0;
        }
        try {
            var token = Integer.parseInt(pageToken);
            if (token < 0) {
                throw new IllegalArgumentException("Invalid page token: " + pageToken);
            }
            return token;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid page token: " + pageToken, e);
        }
    }
}
