package kasanari.catalog.iceberg.core;

import kasanari.catalog.iceberg.core.exception.IcebergCatalogAdapterException;
import kasanari.catalog.iceberg.core.model.IcebergCatalog;
import kasanari.catalog.iceberg.core.model.IcebergNamespace;
import kasanari.catalog.iceberg.core.model.IcebergSnapshot;
import kasanari.catalog.iceberg.core.model.IcebergTable;
import kasanari.catalog.iceberg.core.model.IcebergValues;
import kasanari.catalog.iceberg.core.model.IcebergView;
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
import org.apache.iceberg.rest.requests.UpdateTableRequest;
import org.apache.iceberg.util.Tasks;
import org.apache.iceberg.view.BaseView;
import org.apache.iceberg.view.SQLViewRepresentation;
import org.apache.iceberg.view.View;
import org.apache.iceberg.view.ViewMetadata;
import org.apache.iceberg.view.ViewRepresentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

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
    public void createNamespace(IcebergNamespace namespace) {
        isNamespaceMethodAllowed("createNamespace");
        namespaceCatalog.createNamespace(Namespace.of(namespace.name().levels()), namespace.properties());
    }

    @Override
    public void dropNamespace(IcebergNamespace.Name namespace) {
        isNamespaceMethodAllowed("dropNamespace");
        namespaceCatalog.dropNamespace(Namespace.of(namespace.levels()));
    }

    @Override
    public IcebergNamespace loadNamespaceMetadata(IcebergNamespace.Name namespace) {
        isNamespaceMethodAllowed("loadNamespaceMetadata");
        var metadata = namespaceCatalog.loadNamespaceMetadata(Namespace.of(namespace.levels()));
        return new IcebergNamespace(namespace, metadata);
    }

    @Override
    public boolean namespaceExists(IcebergNamespace.Name namespace) {
        isNamespaceMethodAllowed("namespaceExists");
        return namespaceCatalog.namespaceExists(Namespace.of(namespace.levels()));
    }

    @Override
    public IcebergNamespace.Listing listNamespaces(IcebergNamespace.Listing.Filter filter) {
        isNamespaceMethodAllowed("listNamespaces");
        List<Namespace> namespaces;

        if (filter.parent().isEmpty()) {
            namespaces = namespaceCatalog.listNamespaces();
        } else {
            namespaces = namespaceCatalog.listNamespaces(Namespace.of(filter.parent().get()));
        }

        var pageToken = filter.pageToken().orElse("");
        var start = "".equals(pageToken) ? 0 : Integer.parseInt(pageToken);
        var end = start + filter.pageSize().orElse(0);

        var nextToken = Optional.of(String.valueOf(end));
        var subList = namespaces.subList(start, end).stream().map(it -> new IcebergNamespace.Name(it.levels())).toList();

        if (end >= namespaces.size()) {
            nextToken = Optional.empty();
        }

        return new IcebergNamespace.Listing(subList, nextToken);
    }

    @Override
    public IcebergNamespace updateNamespace(IcebergNamespace.Name namespace, IcebergNamespace.Update rq) {
        isNamespaceMethodAllowed("updateNamespace");
        var removals = rq.removals();
        var updates = rq.updates();

        var namespaceIdentifier = namespace.toIceberg();

        if (!updates.isEmpty()) {
            namespaceCatalog.setProperties(namespaceIdentifier, updates);
        }

        if (!removals.isEmpty()) {
            namespaceCatalog.removeProperties(namespaceIdentifier, removals);
        }

        var updatedProperties = namespaceCatalog.loadNamespaceMetadata(namespaceIdentifier);

        return new IcebergNamespace(namespace, updatedProperties);
    }

    @Override
    public IcebergView.Metadata createView(IcebergView.CreateRequest rq) {
        isViewMethodAllowed("createView");
        var identifier = TableIdentifier.of(Namespace.of(rq.namespace().levels()), rq.name().value());

        var view = viewCatalog
                .buildView(identifier)
                .withLocation(rq.location().value())
                .withProperties(rq.properties())
                .withSchema(rq.schema())
                .withDefaultNamespace(identifier.namespace())
                .create();
        var baseView = asBaseView(view);

        return toIcebergViewMetadata(rq.namespace(), baseView.operations().current());
    }

    @Override
    public boolean viewExists(IcebergNamespace.Name namespace, IcebergView.Name view) {
        isViewMethodAllowed("viewExists");
        var identifier = TableIdentifier.of(Namespace.of(namespace.levels()), view.value());
        return viewCatalog.viewExists(identifier);
    }

    @Override
    public IcebergView.Metadata loadView(IcebergView view) {
        isViewMethodAllowed("loadView");
        var loadedView = asBaseView(viewCatalog.loadView(view.toIceberg()));
        var metadata = loadedView.operations().current();
        return toIcebergViewMetadata(view.namespace(), metadata);
    }

    @Override
    public void renameView(IcebergView from, IcebergView to) {
        isViewMethodAllowed("renameView");
        viewCatalog.renameView(from.toIceberg(), to.toIceberg());
    }

    @Override
    public IcebergView.Listing listViews(IcebergNamespace.Name namespace, IcebergView.Listing.Filter filter) {
        isViewMethodAllowed("listViews");
        var namespaceIdentifier = Namespace.of(namespace.levels());

        var views = viewCatalog.listViews(namespaceIdentifier);

        var pageToken = filter.pageToken().orElse("");
        var start = "".equals(pageToken) ? 0 : Integer.parseInt(pageToken);
        var end = start + filter.pageSize().orElse(0);

        var nextToken = Optional.of(String.valueOf(end));
        var subList = views.subList(start, end).stream().map(it -> new IcebergView(namespace, new IcebergView.Name(it.name()))).toList();

        if (end >= views.size()) {
            nextToken = Optional.empty();
        }

        return new IcebergView.Listing(subList, nextToken);
    }

    @Override
    public void dropView(IcebergView view) {
        isViewMethodAllowed("dropView");
        viewCatalog.dropView(view.toIceberg());
    }

    @Override
    public IcebergView.Metadata replaceView(IcebergView view, IcebergView.UpdateRequest rq) {
        isViewMethodAllowed("replaceView");
        var identifier = view.toIceberg();
        var loadedView = asBaseView(viewCatalog.loadView(identifier));
        var ops = loadedView.operations();

        var updates = rq.toIceberg(identifier);
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

                    var metadataBuilder = ViewMetadata.buildFrom(base);
                    updates.updates().forEach(it -> it.applyTo(metadataBuilder));
                    var updated = metadataBuilder.build();

                    if (!updated.changes().isEmpty()) {
                        // commit only if changes is not empty
                        task.commit(base, updated);
                    }
                });

        var currentMetadata = ops.current();

        return toIcebergViewMetadata(view.namespace(), currentMetadata);
    }

    @Override
    public boolean tableExists(IcebergNamespace.Name namespace, IcebergTable.Name name) {
        var identifier = TableIdentifier.of(Namespace.of(namespace.levels()), name.value());
        return catalog.tableExists(identifier);
    }

    @Override
    public void dropTable(IcebergTable table, boolean purge) {
        catalog.dropTable(table.toIceberg(), purge);
    }

    @Override
    public IcebergTable.Listing listTables(IcebergNamespace.Name namespace, IcebergTable.Listing.Filter filter) {
        var namespaceIdentifier = Namespace.of(namespace.levels());

        var views = catalog.listTables(namespaceIdentifier);

        var pageToken = filter.pageToken().orElse("");
        var start = "".equals(pageToken) ? 0 : Integer.parseInt(pageToken);
        var end = start + filter.pageSize().orElse(0);

        var nextToken = Optional.of(String.valueOf(end));
        var subList = views.subList(start, end).stream().map(it -> new IcebergTable(namespace, new IcebergTable.Name(it.name()))).toList();

        if (end >= views.size()) {
            nextToken = Optional.empty();
        }

        return new IcebergTable.Listing(subList, nextToken);
    }

    @Override
    public void createTable(IcebergTable.CreateRequest request) {
        var identifier = TableIdentifier.of(Namespace.of(request.namespace().levels()), request.name().value());

        catalog
                .buildTable(identifier, request.schema())
                .withLocation(request.location().value())
                .withProperties(request.properties())
                .withPartitionSpec(request.partitionSpecification().toIceberg(request.schema()))
                .withSortOrder(request.sortSpecification().toIceberg(request.schema()))
                .create();
    }

    @Override
    public void renameTable(IcebergTable from, IcebergTable to) {
        catalog.renameTable(from.toIceberg(), to.toIceberg());
    }

    @Override
    public IcebergTable.LoadedTable registerTable(IcebergTable table, IcebergValues.Location location) {
        catalog.registerTable(table.toIceberg(), location.value());

        var loadedTable = asBaseTable(catalog.loadTable(table.toIceberg()));
        var metadata = loadedTable.operations().current();
        return new IcebergTable.LoadedTable(toIcebergTableMetadata(table.namespace(), metadata));
    }

    @Override
    public IcebergTable.LoadedTable loadTable(IcebergTable table) {
        var loadedTable = asBaseTable(catalog.loadTable(table.toIceberg()));
        var metadata = loadedTable.operations().current();
        return new IcebergTable.LoadedTable(toIcebergTableMetadata(table.namespace(), metadata));
    }

    @Override
    public IcebergTable.Commit updateTable(IcebergTable table, IcebergTable.UpdateRequest rq) {
        var identifier = table.toIceberg();
        var loadedTable = asBaseTable(catalog.loadTable(identifier));
        var ops = loadedTable.operations();
        var updates = rq.toIceberg(identifier);

        var currentMetadata = commitTableUpdates(updates, ops);

        return new IcebergTable.Commit(
                new IcebergValues.Location(currentMetadata.location()),
                toIcebergTableMetadata(table.namespace(), currentMetadata)
        );
    }

    @Override
    public void commitTransaction(List<IcebergTable.Transaction> transactions) {
        var awaitingTransactions = new ArrayList<Transaction>();

        transactions.forEach(tx -> {
            var tableIdentifier = tx.table().toIceberg();
            var loadedTable = asBaseTable(catalog.loadTable(tableIdentifier));
            var openedTx = Transactions.newTransaction(tableIdentifier.toString(), loadedTable.operations());
            awaitingTransactions.add(openedTx);

            var updates = tx.changes().toIceberg(tableIdentifier);
            var ops = (BaseTransaction.TransactionTable) openedTx.table();

            commitTableUpdates(updates, ops.operations());
        });

        awaitingTransactions.forEach(Transaction::commitTransaction);
    }

    private TableMetadata commitTableUpdates(UpdateTableRequest updates, TableOperations ops) {
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

    private BaseView asBaseView(View value) {
        return (BaseView) value;
    }

    private BaseTable asBaseTable(Table value) {
        return (BaseTable) value;
    }

    private IcebergView.Metadata toIcebergViewMetadata(IcebergNamespace.Name namespace, ViewMetadata metadata) {
        var versions = metadata.versions().stream().map(it ->
                new IcebergView.Metadata.Version(
                        new IcebergValues.VersionId(it.versionId()),
                        new IcebergValues.Timestamp(it.timestampMillis()),
                        new IcebergValues.SchemaId(it.schemaId()),
                        it.summary(),
                        it.representations().stream().map(repr -> {
                            var sqlRepr = asSQLViewRepresentation(repr);

                            return new IcebergView.Metadata.Version.Representation(
                                    new IcebergView.Metadata.Version.Representation.Type(sqlRepr.type()),
                                    new IcebergView.Metadata.Version.Representation.Sql(sqlRepr.sql()),
                                    new IcebergView.Metadata.Version.Representation.Dialect(sqlRepr.dialect())
                            );
                        }).toList(),
                        Optional.ofNullable(it.defaultCatalog()).map(IcebergCatalog.Name::new),
                        namespace
                )
        ).toList();

        var versionsHistory = metadata.history().stream().map(it -> new IcebergView.Metadata.HistoryEntry(
                new IcebergValues.VersionId(it.versionId()),
                new IcebergValues.Timestamp(it.timestampMillis())
        )).toList();

        return new IcebergView.Metadata(
                new IcebergValues.Uuid(metadata.uuid()),
                new IcebergValues.FormatVersion(metadata.formatVersion()),
                new IcebergValues.Location(metadata.location()),
                new IcebergValues.VersionId(metadata.currentVersionId()),
                versions,
                versionsHistory,
                metadata.schemas(),
                metadata.properties()
        );
    }

    private IcebergTable.Metadata toIcebergTableMetadata(IcebergNamespace.Name namespace, TableMetadata metadata) {
        var partitionSpecifications = metadata.specs().stream().map(it -> {
            if (it.isPartitioned()) {
                return new IcebergTable.PartitionSpecification.Partitioned(
                        Optional.of(new IcebergTable.PartitionSpecification.Id(it.specId())),
                        it
                                .fields()
                                .stream()
                                .map(field -> new IcebergTable.PartitionSpecification.Partitioned.Field(
                                        Optional.of(new IcebergValues.ColumnId(field.fieldId())),
                                        new IcebergValues.SourceId(field.sourceId()),
                                        new IcebergTable.PartitionSpecification.Partitioned.Field.Name(field.name()),
                                        IcebergTable.Transform.fromIceberg(field.transform())

                                ))
                                .toList()
                );
            } else {
                return (IcebergTable.PartitionSpecification) new IcebergTable.PartitionSpecification.Unpartitioned();
            }
        }).toList();

        var sortOrderSpecifications = metadata.sortOrders().stream().map(it -> {
            if (it.isSorted()) {
                return new IcebergTable.SortSpecification.Sorted(
                        new IcebergTable.SortSpecification.Id(it.orderId()),
                        it.fields().stream().map(field -> new IcebergTable.SortSpecification.Sorted.Field(
                                new IcebergValues.SourceId(field.sourceId()),
                                IcebergTable.Transform.fromIceberg(field.transform()),
                                IcebergTable.SortSpecification.Sorted.Direction.valueOf(field.direction().name().toUpperCase()),
                                IcebergTable.SortSpecification.Sorted.NullOrder.valueOf(field.nullOrder().name().toUpperCase())

                        )).toList()
                );
            } else {
                return (IcebergTable.SortSpecification) new IcebergTable.SortSpecification.Unsorted();
            }
        }).toList();

        var snapshots = metadata.snapshots().stream().map(it -> new IcebergSnapshot(
                new IcebergSnapshot.Id(it.snapshotId()),
                Optional.ofNullable(it.parentId()).map(IcebergSnapshot.Id::new),
                new IcebergValues.SequenceNumber(it.sequenceNumber()),
                new IcebergValues.Timestamp(it.timestampMillis()),
                new IcebergValues.Location(it.manifestListLocation()),
                new IcebergSnapshot.Summary(
                        IcebergSnapshot.Summary.Operation.valueOf(it.operation().toUpperCase()),
                        it.summary()
                )
        )).toList();

        var snapshotReferences = new HashMap<String, IcebergSnapshot.Reference>();
        metadata.refs().forEach((id, ref) -> {
            var type = ref.isBranch() ? IcebergSnapshot.Reference.Type.BRANCH : IcebergSnapshot.Reference.Type.TAG;

            snapshotReferences.put(id, new IcebergSnapshot.Reference(
                    type,
                    new IcebergSnapshot.Id(ref.snapshotId()),
                    new IcebergSnapshot.Reference.KeepDuration(ref.maxRefAgeMs()),
                    new IcebergSnapshot.Reference.KeepDuration(ref.maxSnapshotAgeMs()),
                    new IcebergSnapshot.Reference.KeepCount(ref.minSnapshotsToKeep())
            ));
        });

        var snapshotLog = metadata.snapshotLog().stream().map(it -> new IcebergSnapshot.Log(
                new IcebergSnapshot.Id(it.snapshotId()),
                new IcebergValues.Timestamp(it.timestampMillis())
        )).toList();

        var metadataLog = metadata.previousFiles().stream().map(it ->
                new IcebergTable.Metadata.Log(
                        new IcebergValues.Location(it.file()),
                        new IcebergValues.Timestamp(it.timestampMillis())
                )
        ).toList();

        var statistics = metadata.statisticsFiles().stream().map(it ->
                new IcebergSnapshot.Statistics(
                        new IcebergSnapshot.Id(it.snapshotId()),
                        new IcebergValues.Location(it.path()),
                        new IcebergValues.ByteSize(it.fileSizeInBytes()),
                        new IcebergValues.ByteSize(it.fileFooterSizeInBytes()),
                        it.blobMetadata().stream().map(blob -> new IcebergSnapshot.Statistics.BlobMetadata(
                                new IcebergSnapshot.Statistics.BlobMetadata.Type(blob.type()),
                                new IcebergSnapshot.Id(blob.sourceSnapshotId()),
                                new IcebergValues.SequenceNumber(blob.sourceSnapshotSequenceNumber()),
                                blob.fields().stream().map(IcebergSnapshot.Statistics.BlobMetadata.Field::new).toList(),
                                blob.properties()
                        )).toList()
                )
        ).toList();

        var partitionStatistics = metadata.partitionStatisticsFiles().stream().map(it ->
                new IcebergSnapshot.PartitionStatistics(
                        new IcebergSnapshot.Id(it.snapshotId()),
                        new IcebergValues.Location(it.path()),
                        new IcebergValues.ByteSize(it.fileSizeInBytes())
                )).toList();

        return new IcebergTable.Metadata(
                new IcebergValues.FormatVersion(metadata.formatVersion()),
                new IcebergValues.Uuid(metadata.uuid()),
                new IcebergValues.Location(metadata.location()),
                new IcebergValues.Timestamp(metadata.lastUpdatedMillis()),
                metadata.properties(),
                metadata.schemas(),
                new IcebergValues.SchemaId(metadata.currentSchemaId()),
                new IcebergValues.ColumnId(metadata.lastColumnId()),
                partitionSpecifications,
                new IcebergTable.PartitionSpecification.Id(metadata.defaultSpecId()),
                new IcebergTable.PartitionSpecification.Id(metadata.lastAssignedPartitionId()),
                sortOrderSpecifications,
                new IcebergTable.SortSpecification.Id(metadata.defaultSortOrderId()),
                snapshots,
                snapshotReferences,
                new IcebergSnapshot.Id(metadata.currentSnapshot().snapshotId()),
                new IcebergValues.SequenceNumber(metadata.lastSequenceNumber()),
                snapshotLog,
                metadataLog,
                statistics,
                partitionStatistics
        );
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
