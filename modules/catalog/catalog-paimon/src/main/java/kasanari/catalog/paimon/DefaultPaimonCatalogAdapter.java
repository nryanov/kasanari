package kasanari.catalog.paimon;

import kasanari.core.ThrowableRunnable;
import kasanari.core.ThrowableSupplier;
import org.apache.paimon.Snapshot;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.catalog.PropertyChange;
import org.apache.paimon.function.FunctionImpl;
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
import org.apache.paimon.schema.Schema;
import org.apache.paimon.table.AppendOnlyFileStoreTable;
import org.apache.paimon.table.PrimaryKeyFileStoreTable;
import org.apache.paimon.view.ViewImpl;
import org.apache.paimon.view.ViewSchema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class DefaultPaimonCatalogAdapter implements PaimonCatalogAdapter {
    private final Catalog catalog;

    public DefaultPaimonCatalogAdapter(Catalog catalog) {
        this.catalog = catalog;
    }

    @Override
    public ListDatabasesResponse listDatabases(Integer maxResults, String pageToken) {
        var paged = call(() -> catalog.listDatabasesPaged(maxResults, pageToken, null));
        return new ListDatabasesResponse(paged.getElements(), paged.getNextPageToken());
    }

    @Override
    public void createDatabase(CreateDatabaseRequest request) {
        run(() -> catalog.createDatabase(request.getName(), false, mapOrEmpty(request.getOptions())));
    }

    @Override
    public GetDatabaseResponse getDatabase(String database) {
        var loaded = call(() -> catalog.getDatabase(database));
        return new GetDatabaseResponse(
                loaded.name(),
                loaded.name(),
                null,
                mapOrEmpty(loaded.options()),
                null,
                0L,
                null,
                0L,
                null
        );
    }

    @Override
    public void dropDatabase(String database) {
        run(() -> catalog.dropDatabase(database, false, false));
    }

    @Override
    public AlterDatabaseResponse alterDatabase(String database, AlterDatabaseRequest request) {
        var changes = new ArrayList<PropertyChange>();

        var updates = mapOrEmpty(request.getUpdates());
        var removals = new HashSet<>(listOrEmpty(request.getRemovals()));

        var filteredUpdates = new HashMap<>(updates);
        for (var update : updates.entrySet()) {
            if (!removals.contains(update.getKey())) {
                filteredUpdates.put(update.getKey(), update.getValue());
                changes.add(PropertyChange.setProperty(update.getKey(), update.getValue()));
            }
        }

        for (var removal : removals) {
            changes.add(PropertyChange.removeProperty(removal));
        }

        return call(() -> {
            var db = catalog.getDatabase(database);
            var currentOptions = db.options();

            catalog.alterDatabase(database, changes, false);

            var missingProperties = removals.stream()
                    .filter(key -> !currentOptions.containsKey(key))
                    .toList();

            var removed = new HashSet<>(removals);
            removed.retainAll(currentOptions.keySet());

            return new AlterDatabaseResponse(
                    removed.stream().toList(),
                    filteredUpdates.keySet().stream().toList(),
                    missingProperties
            );
        });
    }

    @Override
    public void registerTable(String database, RegisterTableRequest request) {
        run(() -> catalog.registerTable(request.getIdentifier(), request.getPath()));
    }

    @Override
    public ListTablesResponse listTables(String database, Integer maxResults, String pageToken, String tableNamePattern) {
        var paged = call(() -> catalog.listTablesPaged(database, maxResults, pageToken, tableNamePattern, null));
        return new ListTablesResponse(paged.getElements(), paged.getNextPageToken());
    }

    @Override
    public void createTable(String database, CreateTableRequest request) {
        run(() -> catalog.createTable(request.getIdentifier(), request.getSchema(), false));
    }

    @Override
    public ListTableDetailsResponse listTableDetails(String database, Integer maxResults, String pageToken, String tableNamePattern, String tableType) {
        var paged = call(() -> catalog.listTableDetailsPaged(database, maxResults, pageToken, tableNamePattern, tableType));
        var tableDetails = new ArrayList<GetTableResponse>();

        for (var table : paged.getElements()) {
            var schemaId = table.latestSnapshot().map(Snapshot::schemaId).orElse(-1L);

            var it = new GetTableResponse(
                    table.uuid(),
                    database,
                    table.name(),
                    table.options().get("path"),
                    false,
                    schemaId,
                    null,
                    null,
                    0L,
                    null,
                    0L,
                    null
            );

            tableDetails.add(it);
        }
        return new ListTableDetailsResponse(tableDetails, paged.getNextPageToken());
    }

    @Override
    public ListTablesGloballyResponse listTablesGlobally(String databaseNamePattern, String tableNamePattern, Integer maxResults, String pageToken) {
        var paged = call(() -> catalog.listTablesPagedGlobally(databaseNamePattern, tableNamePattern, maxResults, pageToken));
        return new ListTablesGloballyResponse(paged.getElements(), paged.getNextPageToken());
    }

    @Override
    public GetTableResponse getTableById(String tableId) {
        var table = call(() -> catalog.getTableById(tableId));

        long schemaId = -1;
        Schema schema = null;

        var database = "";
        var name = table.name();

        var fullName = table.fullName();
        var tableNameParts = fullName.split("[.]");

        if (tableNameParts.length > 1) {
            database = tableNameParts[0];
        }


        if (table instanceof PrimaryKeyFileStoreTable) {
            schema = ((PrimaryKeyFileStoreTable) table).schema().toSchema();
            schemaId = ((PrimaryKeyFileStoreTable) table).schema().id();
        }

        if (table instanceof AppendOnlyFileStoreTable) {
            schema = ((AppendOnlyFileStoreTable) table).schema().toSchema();
            schemaId = ((AppendOnlyFileStoreTable) table).schema().id();
        }

        return new GetTableResponse(
                tableId,
                database,
                name,
                table.options().get("path"),
                false,
                schemaId,
                schema,
                null,
                0L,
                null,
                0L,
                null
        );
    }

    @Override
    public GetTableResponse getTable(String database, String table) {
        var loaded = call(() -> catalog.getTable(Identifier.create(database, table)));

        long schemaId = -1;
        Schema schema = null;

        if (loaded instanceof PrimaryKeyFileStoreTable) {
            schema = ((PrimaryKeyFileStoreTable) loaded).schema().toSchema();
            schemaId = ((PrimaryKeyFileStoreTable) loaded).schema().id();
        }

        if (loaded instanceof AppendOnlyFileStoreTable) {
            schema = ((AppendOnlyFileStoreTable) loaded).schema().toSchema();
            schemaId = ((AppendOnlyFileStoreTable) loaded).schema().id();
        }

        return new GetTableResponse(
                loaded.uuid(),
                database,
                loaded.name(),
                loaded.options().get("path"),
                false,
                schemaId,
                schema,
                null,
                0L,
                null,
                0L,
                null
        );
    }

    @Override
    public void alterTable(String database, String table, AlterTableRequest request) {
        run(() -> catalog.alterTable(Identifier.create(database, table), listOrEmpty(request.getChanges()), false));
    }

    @Override
    public void dropTable(String database, String table) {
        run(() -> catalog.dropTable(Identifier.create(database, table), false));
    }

    @Override
    public void renameTable(RenameTableRequest request) {
        run(() -> catalog.renameTable(request.getSource(), request.getDestination(), false));
    }

    @Override
    public CommitTableResponse commitTable(String database, String table, CommitTableRequest request) {
        var success = call(() -> catalog.commitSnapshot(
                Identifier.create(database, table),
                request.getTableId(),
                request.getSnapshot(),
                listOrEmpty(request.getStatistics())
        ));

        return new CommitTableResponse(success);
    }

    @Override
    public void rollbackTable(String database, String table, RollbackTableRequest request) {
        run(() -> catalog.rollbackTo(Identifier.create(database, table), request.getInstant(), request.getFromSnapshot()));
    }

    @Override
    public void rollbackSchema(String database, String table, RollbackSchemaRequest request) {
        run(() -> catalog.rollbackSchema(Identifier.create(database, table), request.getSchemaId()));
    }

    @Override
    public GetTableTokenResponse getTableToken(String database, String table) {
        throw new UnsupportedOperationException("Current catalog does not support loading table token.");
    }

    @Override
    public AuthTableQueryResponse authTableQuery(String database, String table, AuthTableQueryRequest request) {
        throw new UnsupportedOperationException("Current catalog does not support loading table token.");
    }

    @Override
    public GetTableSnapshotResponse getTableSnapshot(String database, String table) {
        var snapshot = call(() -> catalog.loadSnapshot(Identifier.create(database, table)));
        return new GetTableSnapshotResponse(snapshot.orElse(null));
    }

    @Override
    public GetVersionSnapshotResponse getVersionSnapshot(String database, String table, String version) {
        return new GetVersionSnapshotResponse(call(() -> catalog.loadSnapshot(Identifier.create(database, table), version)).orElse(null));
    }

    @Override
    public ListSnapshotsResponse listSnapshots(String database, String table, Integer maxResults, String pageToken) {
        var paged = call(() -> catalog.listSnapshotsPaged(Identifier.create(database, table), maxResults, pageToken));
        return new ListSnapshotsResponse(paged.getElements(), paged.getNextPageToken());
    }

    @Override
    public ListPartitionsResponse listPartitions(String database, String table, Integer maxResults, String pageToken, String partitionNamePattern) {
        var paged = call(() -> catalog.listPartitionsPaged(
                Identifier.create(database, table),
                maxResults,
                pageToken,
                partitionNamePattern
        ));

        return new ListPartitionsResponse(paged.getElements(), paged.getNextPageToken());
    }

    @Override
    public void markDonePartitions(String database, String table, MarkDonePartitionsRequest request) {
        run(() -> catalog.markDonePartitions(Identifier.create(database, table), request.getPartitionSpecs()));
    }

    @Override
    public ListPartitionsResponse listPartitionsByNames(String database, String table, ListPartitionsByNamesRequest request) {
        var partitions = call(() -> catalog.listPartitionsByNames(Identifier.create(database, table), request.getPartitionSpecs()));
        return new ListPartitionsResponse(partitions, null);
    }

    @Override
    public ListBranchesResponse listBranches(String database, String table) {
        return new ListBranchesResponse(call(() -> catalog.listBranches(Identifier.create(database, table))));
    }

    @Override
    public void createBranch(String database, String table, CreateBranchRequest request) {
        run(() -> catalog.createBranch(Identifier.create(database, table), request.branch(), request.fromTag()));
    }

    @Override
    public void dropBranch(String database, String table, String branch) {
        run(() -> catalog.dropBranch(Identifier.create(database, table), branch));
    }

    @Override
    public void renameBranch(String database, String table, String branch, RenameBranchRequest request) {
        run(() -> catalog.renameBranch(Identifier.create(database, table), branch, request.toBranch()));
    }

    @Override
    public void forwardBranch(String database, String table, String branch, ForwardBranchRequest request) {
        run(() -> catalog.fastForward(Identifier.create(database, table), branch));
    }

    @Override
    public ListTagsResponse listTags(String database, String table, Integer maxResults, String pageToken, String tagNamePrefix) {
        var paged = call(() -> catalog.listTagsPaged(
                Identifier.create(database, table),
                maxResults,
                pageToken,
                tagNamePrefix
        ));
        return new ListTagsResponse(paged.getElements(), paged.getNextPageToken());
    }

    @Override
    public void createTag(String database, String table, CreateTagRequest request) {
        run(() -> catalog.createTag(
                Identifier.create(database, table),
                request.tagName(),
                request.snapshotId(),
                request.timeRetained(),
                false
        ));
    }

    @Override
    public GetTagResponse getTag(String database, String table, String tag) {
        return call(() -> catalog.getTag(Identifier.create(database, table), tag));
    }

    @Override
    public void deleteTag(String database, String table, String tag) {
        run(() -> catalog.deleteTag(Identifier.create(database, table), tag));
    }

    @Override
    public ListConsumersResponse listConsumers(String database, String table, Integer maxResults, String pageToken) {
        var paged = call(() -> catalog.listConsumersPaged(Identifier.create(database, table), maxResults, pageToken));
        return new ListConsumersResponse(paged.getElements(), paged.getNextPageToken());
    }

    @Override
    public void resetConsumer(String database, String table, ResetConsumerRequest request) {
        run(() -> catalog.resetConsumer(Identifier.create(database, table), request.consumerId(), request.nextSnapshotId()));
    }

    @Override
    public ListViewsResponse listViews(String database, Integer maxResults, String pageToken, String viewNamePattern) {
        var paged = call(() -> catalog.listViewsPaged(database, maxResults, pageToken, viewNamePattern));
        return new ListViewsResponse(paged.getElements(), paged.getNextPageToken());
    }

    @Override
    public void createView(String database, CreateViewRequest request) {
        var schema = request.getSchema();
        var view = new ViewImpl(
                request.getIdentifier(),
                schema.fields(),
                schema.query(),
                schema.dialects(),
                schema.comment(),
                schema.options()
        );
        run(() -> catalog.createView(request.getIdentifier(), view, false));
    }

    @Override
    public ListViewDetailsResponse listViewDetails(String database, Integer maxResults, String pageToken, String viewNamePattern) {
        var paged = call(() -> catalog.listViewDetailsPaged(database, maxResults, pageToken, viewNamePattern));
        var viewDetails = new ArrayList<GetViewResponse>();

        for (var view : paged.getElements()) {
            var schema = new ViewSchema(
                    view.rowType().getFields(),
                    view.query(),
                    view.dialects(),
                    view.comment().orElse(null),
                    view.options()
            );

            var it = new GetViewResponse(
                    view.fullName(),
                    view.name(),
                    schema,
                    null,
                    -1L,
                    null,
                    -1L,
                    null
            );

            viewDetails.add(it);
        }
        return new ListViewDetailsResponse(viewDetails, paged.getNextPageToken());
    }

    @Override
    public ListViewsGloballyResponse listViewsGlobally(String databaseNamePattern, String viewNamePattern, Integer maxResults, String pageToken) {
        var paged = call(() -> catalog.listViewsPagedGlobally(databaseNamePattern, viewNamePattern, maxResults, pageToken));
        return new ListViewsGloballyResponse(paged.getElements(), paged.getNextPageToken());
    }

    @Override
    public GetViewResponse getView(String database, String view) {
        var loaded = call(() -> catalog.getView(Identifier.create(database, view)));
        var schema = new ViewSchema(
                loaded.rowType().getFields(),
                loaded.query(),
                loaded.dialects(),
                loaded.comment().orElse(null),
                loaded.options()
        );
        return new GetViewResponse(
                Identifier.create(database, view).getFullName(),
                view,
                schema,
                null,
                0L,
                null,
                0L,
                null
        );
    }

    @Override
    public void alterView(String database, String view, AlterViewRequest request) {
        run(() -> catalog.alterView(Identifier.create(database, view), listOrEmpty(request.viewChanges()), false));
    }

    @Override
    public void dropView(String database, String view) {
        run(() -> catalog.dropView(Identifier.create(database, view), false));
    }

    @Override
    public void renameView(RenameTableRequest request) {
        run(() -> catalog.renameView(request.getSource(), request.getDestination(), false));
    }

    @Override
    public ListFunctionsResponse listFunctions(String database, Integer maxResults, String pageToken, String functionNamePattern) {
        var paged = call(() -> catalog.listFunctionsPaged(database, maxResults, pageToken, functionNamePattern));
        return new ListFunctionsResponse(paged.getElements(), paged.getNextPageToken());
    }

    @Override
    public void createFunction(String database, CreateFunctionRequest request) {
        var identifier = Identifier.create(database, request.name());
        var function = new FunctionImpl(
                identifier,
                request.inputParams(),
                request.returnParams(),
                request.isDeterministic(),
                request.definitions(),
                request.comment(),
                request.options()
        );
        run(() -> catalog.createFunction(identifier, function, false));
    }

    @Override
    public ListFunctionDetailsResponse listFunctionDetails(String database, Integer maxResults, String pageToken, String functionNamePattern) {
        var paged = call(() -> catalog.listFunctionDetailsPaged(database, maxResults, pageToken, functionNamePattern));
        var functionDetails = new ArrayList<GetFunctionResponse>();

        for (var function : paged.getElements()) {

            var it = new GetFunctionResponse(
                    function.fullName(),
                    function.name(),
                    function.inputParams().orElse(null),
                    function.returnParams().orElse(null),
                    function.isDeterministic(),
                    function.definitions(),
                    function.comment(),
                    function.options(),
                    null,
                    0L,
                    null,
                    0L,
                    null
            );

            functionDetails.add(it);
        }

        return new ListFunctionDetailsResponse(functionDetails, paged.getNextPageToken());
    }

    @Override
    public ListFunctionsGloballyResponse listFunctionsGlobally(String databaseNamePattern, String functionNamePattern, Integer maxResults, String pageToken) {
        var paged = call(() -> catalog.listFunctionsPagedGlobally(databaseNamePattern, functionNamePattern, maxResults, pageToken));
        return new ListFunctionsGloballyResponse(paged.getElements(), paged.getNextPageToken());
    }

    @Override
    public GetFunctionResponse getFunction(String database, String function) {
        var loaded = call(() -> catalog.getFunction(Identifier.create(database, function)));
        return new GetFunctionResponse(
                Identifier.create(database, function).getFullName(),
                loaded.name(),
                loaded.inputParams().orElse(null),
                loaded.returnParams().orElse(null),
                loaded.isDeterministic(),
                loaded.definitions(),
                loaded.comment(),
                loaded.options(),
                null,
                0L,
                null,
                0L,
                null
        );
    }

    @Override
    public void alterFunction(String database, String function, AlterFunctionRequest request) {
        run(() -> catalog.alterFunction(Identifier.create(database, function), request.changes(), false));
    }

    @Override
    public void dropFunction(String database, String function) {
        run(() -> catalog.dropFunction(Identifier.create(database, function), false));
    }

    private Map<String, String> mapOrEmpty(Map<String, String> map) {
        return map == null ? Collections.emptyMap() : map;
    }

    private <T> List<T> listOrEmpty(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }

    private void run(ThrowableRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T call(ThrowableSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Catalog getUnderlyingCatalog() {
        return catalog;
    }
}
