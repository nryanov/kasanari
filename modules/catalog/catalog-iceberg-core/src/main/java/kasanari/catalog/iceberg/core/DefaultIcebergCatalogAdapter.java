package kasanari.catalog.iceberg.core;

import kasanari.catalog.iceberg.core.exception.IcebergCatalogAdapterException;
import org.apache.iceberg.BaseTable;
import org.apache.iceberg.BaseTransaction;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableMetadata;
import org.apache.iceberg.TableOperations;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.Transactions;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.SupportsNamespaces;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.catalog.ViewCatalog;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.rest.requests.CreateTableRequest;
import org.apache.iceberg.rest.requests.CreateViewRequest;
import org.apache.iceberg.rest.requests.UpdateTableRequest;
import org.apache.iceberg.rest.responses.CreateNamespaceResponse;
import org.apache.iceberg.rest.responses.GetNamespaceResponse;
import org.apache.iceberg.rest.responses.ImmutableLoadViewResponse;
import org.apache.iceberg.rest.responses.ListNamespacesResponse;
import org.apache.iceberg.rest.responses.ListTablesResponse;
import org.apache.iceberg.rest.responses.LoadTableResponse;
import org.apache.iceberg.rest.responses.LoadViewResponse;
import org.apache.iceberg.rest.responses.UpdateNamespacePropertiesResponse;
import org.apache.iceberg.util.Tasks;
import org.apache.iceberg.view.BaseView;
import org.apache.iceberg.view.SQLViewRepresentation;
import org.apache.iceberg.view.View;
import org.apache.iceberg.view.ViewMetadata;
import org.apache.iceberg.view.ViewRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.apache.iceberg.TableProperties.COMMIT_MAX_RETRY_WAIT_MS_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_MIN_RETRY_WAIT_MS_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_NUM_RETRIES_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_TOTAL_RETRY_TIME_MS_DEFAULT;

public class DefaultIcebergCatalogAdapter implements IcebergCatalogAdapter {
    private final Catalog catalog;
    private final SupportsNamespaces namespaceCatalog;
    private final ViewCatalog viewCatalog;

    public DefaultIcebergCatalogAdapter(Catalog catalog) {
        this.catalog = catalog;
        this.namespaceCatalog = catalog instanceof SupportsNamespaces ? (SupportsNamespaces) catalog : null;
        this.viewCatalog = catalog instanceof ViewCatalog ? (ViewCatalog) catalog : null;
    }

    @Override
    public CreateNamespaceResponse createNamespace(Namespace namespace, Map<String, String> properties) {
        isNamespaceMethodAllowed("createNamespace");
        namespaceCatalog.createNamespace(namespace, properties);

        return CreateNamespaceResponse
                .builder()
                .withNamespace(namespace)
                .setProperties(properties)
                .build();
    }

    @Override
    public void dropNamespace(Namespace namespace) {
        isNamespaceMethodAllowed("dropNamespace");
        namespaceCatalog.dropNamespace(Namespace.of(namespace.levels()));
    }

    @Override
    public GetNamespaceResponse loadNamespaceMetadata(Namespace namespace) {
        isNamespaceMethodAllowed("loadNamespaceMetadata");
        var metadata = namespaceCatalog.loadNamespaceMetadata(Namespace.of(namespace.levels()));
        return GetNamespaceResponse
                .builder()
                .withNamespace(namespace)
                .setProperties(metadata)
                .build();
    }

    @Override
    public boolean namespaceExists(Namespace namespace) {
        isNamespaceMethodAllowed("namespaceExists");
        return namespaceCatalog.namespaceExists(Namespace.of(namespace.levels()));
    }

    @Override
    public ListNamespacesResponse listNamespaces(String pageToken, Integer pageSize, String parent) {
        isNamespaceMethodAllowed("listNamespaces");
        List<Namespace> namespaces;

        if (parent == null || parent.isEmpty()) {
            namespaces = namespaceCatalog.listNamespaces();
        } else {
            namespaces = namespaceCatalog.listNamespaces(Namespace.of(parent.split("[.]")));
        }

        pageToken = pageToken == null ? "" : pageToken;
        pageSize = pageSize == null ? 0 : Math.max(0, pageSize);

        var start = Math.min(namespaces.size(), pageToken.isEmpty() ? 0 : Integer.parseInt(pageToken));
        var end = Math.min(namespaces.size(), start + pageSize);

        var nextToken = String.valueOf(end);
        var subList = namespaces.subList(start, end);

        if (end >= namespaces.size()) {
            nextToken = null;
        }

        return ListNamespacesResponse
                .builder()
                .nextPageToken(nextToken)
                .addAll(subList)
                .build();
    }

    @Override
    public UpdateNamespacePropertiesResponse updateNamespace(Namespace namespace, Map<String, String> updates, Set<String> removals) {
        isNamespaceMethodAllowed("updateNamespace");

        if (!updates.isEmpty()) {
            namespaceCatalog.setProperties(namespace, updates);
        }

        if (!removals.isEmpty()) {
            namespaceCatalog.removeProperties(namespace, removals);
        }

        // todo: build diff
        var updatedProperties = namespaceCatalog.loadNamespaceMetadata(namespace);

        return UpdateNamespacePropertiesResponse
                .builder()
                .addUpdated(updates.keySet())
                .addRemoved(removals)
                .build();
    }

    @Override
    public LoadViewResponse createView(Namespace namespace, CreateViewRequest rq) {
        isViewMethodAllowed("createView");
        var identifier = TableIdentifier.of(namespace, rq.name());

        var viewBuilder = viewCatalog
                .buildView(identifier)
                .withLocation(rq.location())
                .withProperties(rq.properties())
                .withSchema(rq.schema())
                .withDefaultNamespace(identifier.namespace());


        var icebergViewVersion = rq.viewVersion();

        var unsupportedRepresentations =
                icebergViewVersion.representations().stream()
                        .filter(r -> !(r instanceof SQLViewRepresentation))
                        .map(ViewRepresentation::type)
                        .collect(Collectors.toSet());

        // todo: domain error
        if (!unsupportedRepresentations.isEmpty()) {
            throw new IllegalStateException(
                    String.format("Found unsupported view representations: %s", unsupportedRepresentations));
        }

        icebergViewVersion.representations().stream()
                .filter(SQLViewRepresentation.class::isInstance)
                .map(SQLViewRepresentation.class::cast)
                .forEach(it -> viewBuilder.withQuery(it.dialect(), it.sql()));

        var baseView = asBaseView(viewBuilder.create());
        var metadata = baseView.operations().current();

        return ImmutableLoadViewResponse
                .builder()
                .metadata(baseView.operations().current())
                .metadataLocation(metadata.location())
                .build();
    }

    @Override
    public boolean viewExists(TableIdentifier view) {
        isViewMethodAllowed("viewExists");
        return viewCatalog.viewExists(view);
    }

    @Override
    public LoadViewResponse loadView(TableIdentifier view) {
        isViewMethodAllowed("loadView");
        var loadedView = asBaseView(viewCatalog.loadView(view));
        var metadata = loadedView.operations().current();

        return ImmutableLoadViewResponse
                .builder()
                .metadata(metadata)
                .metadataLocation(metadata.location())
                .build();
    }

    @Override
    public void renameView(TableIdentifier from, TableIdentifier to) {
        isViewMethodAllowed("renameView");
        viewCatalog.renameView(from, to);
    }

    @Override
    public ListTablesResponse listViews(Namespace namespace, String pageToken, Integer pageSize) {
        isViewMethodAllowed("listViews");
        var namespaceIdentifier = Namespace.of(namespace.levels());

        var views = viewCatalog.listViews(namespaceIdentifier);

        pageToken = pageToken == null ? "" : pageToken;
        pageSize = pageSize == null ? 0 : pageSize;

        var start = Math.min(views.size(), pageToken.isEmpty() ? 0 : Integer.parseInt(pageToken));
        var end = Math.min(views.size(), start + pageSize);

        var nextToken = String.valueOf(end);
        var subList = views.subList(start, end);

        if (end >= views.size()) {
            nextToken = null;
        }

        return ListTablesResponse
                .builder()
                .addAll(subList)
                .nextPageToken(nextToken)
                .build();
    }

    @Override
    public void dropView(TableIdentifier view) {
        isViewMethodAllowed("dropView");
        viewCatalog.dropView(view);
    }

    @Override
    public LoadViewResponse replaceView(TableIdentifier view, UpdateTableRequest rq) {
        isViewMethodAllowed("replaceView");
        var loadedView = asBaseView(viewCatalog.loadView(view));
        var ops = loadedView.operations();

        var isRetry = new AtomicBoolean(false);
        Tasks.foreach(ops)
                .retry(COMMIT_NUM_RETRIES_DEFAULT) // todo: configure
                .exponentialBackoff(
                        COMMIT_MIN_RETRY_WAIT_MS_DEFAULT,
                        COMMIT_MAX_RETRY_WAIT_MS_DEFAULT,
                        COMMIT_TOTAL_RETRY_TIME_MS_DEFAULT,
                        2.0
                ) // todo: configure
                .onlyRetryOn(CommitFailedException.class)
                .run(task -> {
                    var base = isRetry.get() ? task.refresh() : task.current();
                    isRetry.set(true);

                    // validate request
                    try {
                        rq.requirements().forEach(it -> it.validate(base));
                    } catch (CommitFailedException e) {
                        // to avoid unnecessary retry
                        throw new IllegalStateException("Unsatisfied requirement"); // todo: domain error
                    }

                    var metadataBuilder = ViewMetadata.buildFrom(base);
                    rq.updates().forEach(it -> it.applyTo(metadataBuilder));
                    var updated = metadataBuilder.build();

                    if (!updated.changes().isEmpty()) {
                        // commit only if changes is not empty
                        task.commit(base, updated);
                    }
                });

        var currentMetadata = ops.current();

        return ImmutableLoadViewResponse
                .builder()
                .metadata(currentMetadata)
                .metadataLocation(currentMetadata.location())
                .build();
    }

    @Override
    public boolean tableExists(TableIdentifier table) {
        return catalog.tableExists(table);
    }

    @Override
    public void dropTable(TableIdentifier table, boolean purge) {
        catalog.dropTable(table, purge);
    }

    @Override
    public ListTablesResponse listTables(Namespace namespace, String pageToken, Integer pageSize) {
        var namespaceIdentifier = Namespace.of(namespace.levels());

        var tables = catalog.listTables(namespaceIdentifier);

        pageToken = pageToken == null ? "" : pageToken;
        pageSize = pageSize == null ? 0 : pageSize;
        var start = Math.min(tables.size(), pageToken.isEmpty() ? 0 : Integer.parseInt(pageToken));
        var end = Math.min(tables.size(), start + pageSize);

        var nextToken = String.valueOf(end);
        var subList = tables.subList(start, end);

        if (end >= tables.size()) {
            nextToken = null;
        }

        return ListTablesResponse
                .builder()
                .addAll(subList)
                .nextPageToken(nextToken)
                .build();
    }

    @Override
    public LoadTableResponse createTable(Namespace namespace, CreateTableRequest rq) {
        var identifier = TableIdentifier.of(namespace, rq.name());

        var createdTable = catalog
                .buildTable(identifier, rq.schema())
                .withLocation(rq.location())
                .withProperties(rq.properties())
                .withPartitionSpec(rq.spec())
                .withSortOrder(rq.writeOrder())
                .create();

        var icebergTable = asBaseTable(createdTable);
        var metadata = icebergTable.operations().current();
        return LoadTableResponse
                .builder()
                .withTableMetadata(metadata)
                .build();
    }

    @Override
    public void renameTable(TableIdentifier from, TableIdentifier to) {
        catalog.renameTable(from, to);
    }

    @Override
    public LoadTableResponse registerTable(TableIdentifier table, String location) {
        catalog.registerTable(table, location);

        var loadedTable = asBaseTable(catalog.loadTable(table));
        var metadata = loadedTable.operations().current();
        return LoadTableResponse
                .builder()
                .withTableMetadata(metadata)
                .build();
    }

    @Override
    public LoadTableResponse updateTable(TableIdentifier table, UpdateTableRequest rq) {
        var loadedTable = asBaseTable(catalog.loadTable(table));
        var ops = loadedTable.operations();

        var currentMetadata = commitTableUpdates(rq, ops);

        return LoadTableResponse
                .builder()
                .withTableMetadata(currentMetadata)
                .build();
    }

    @Override
    public LoadTableResponse loadTable(TableIdentifier table) {
        var loadedTable = asBaseTable(catalog.loadTable(table));
        var metadata = loadedTable.operations().current();

        return LoadTableResponse
                .builder()
                .withTableMetadata(metadata)
                .build();
    }

    @Override
    public void commitTransaction(List<UpdateTableRequest> transactions) {
        var awaitingTransactions = new ArrayList<Transaction>();

        transactions.forEach(tx -> {
            var tableIdentifier = tx.identifier();
            var loadedTable = asBaseTable(catalog.loadTable(tableIdentifier));
            var openedTx = Transactions.newTransaction(tableIdentifier.toString(), loadedTable.operations());
            awaitingTransactions.add(openedTx);

            var ops = (BaseTransaction.TransactionTable) openedTx.table();

            commitTableUpdates(tx, ops.operations());
        });

        awaitingTransactions.forEach(Transaction::commitTransaction);
    }

    @Override
    public Catalog delegate() {
        return catalog;
    }

    protected TableMetadata commitTableUpdates(UpdateTableRequest updates, TableOperations ops) {
        var isRetry = new AtomicBoolean(false);
        Tasks.foreach(ops)
                .retry(COMMIT_NUM_RETRIES_DEFAULT) // todo: configure
                .exponentialBackoff(
                        COMMIT_MIN_RETRY_WAIT_MS_DEFAULT,
                        COMMIT_MAX_RETRY_WAIT_MS_DEFAULT,
                        COMMIT_TOTAL_RETRY_TIME_MS_DEFAULT,
                        2.0
                ) // todo: configure
                .onlyRetryOn(CommitFailedException.class)
                .run(task -> {
                    var base = isRetry.get() ? task.refresh() : task.current();
                    isRetry.set(true);

                    // validate request
                    try {
                        updates.requirements().forEach(it -> it.validate(base));
                    } catch (CommitFailedException e) {
                        // to avoid unnecessary retry
                        throw new IllegalStateException("Unsatisfied requirement"); // todo: domain error
                    }

                    var metadataBuilder = TableMetadata.buildFrom(base);
                    updates.updates().forEach(it -> it.applyTo(metadataBuilder));
                    var updated = metadataBuilder.build();

                    if (!updated.changes().isEmpty()) {
                        // commit only if changes is not empty
                        task.commit(base, updated);
                    }
                });

        return ops.current();
    }

    private SQLViewRepresentation asSQLViewRepresentation(ViewRepresentation value) {
        return (SQLViewRepresentation) value;
    }

    protected BaseView asBaseView(View value) {
        return (BaseView) value;
    }

    protected BaseTable asBaseTable(Table value) {
        return (BaseTable) value;
    }

    private void isViewMethodAllowed(String method) {
        if (viewCatalog == null) {
            throw IcebergCatalogAdapterException.UnsupportedMethod.view(method);
        }
    }

    private void isNamespaceMethodAllowed(String method) {
        if (namespaceCatalog == null) {
            throw IcebergCatalogAdapterException.UnsupportedMethod.namespace(method);
        }
    }
}
