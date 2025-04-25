package kasanari.catalog.iceberg.nessie;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.model.IcebergCatalog;
import kasanari.catalog.iceberg.core.model.IcebergNamespace;
import kasanari.catalog.iceberg.core.model.IcebergView;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.CommitFailedException;
import org.apache.iceberg.nessie.NessieCatalog;
import org.apache.iceberg.util.Tasks;
import org.apache.iceberg.view.BaseView;
import org.apache.iceberg.view.SQLViewRepresentation;
import org.apache.iceberg.view.View;
import org.apache.iceberg.view.ViewMetadata;
import org.apache.iceberg.view.ViewRepresentation;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.iceberg.TableProperties.COMMIT_MAX_RETRY_WAIT_MS_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_MIN_RETRY_WAIT_MS_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_NUM_RETRIES_DEFAULT;
import static org.apache.iceberg.TableProperties.COMMIT_TOTAL_RETRY_TIME_MS_DEFAULT;


public class IcebergNessieCatalog implements IcebergCatalogAdapter {
    private final NessieCatalog catalog;

    public IcebergNessieCatalog() {
        // todo: configure
        this.catalog = new NessieCatalog();
    }

    @Override
    public void createNamespace(IcebergNamespace namespace) {
        catalog.createNamespace(Namespace.of(namespace.name().levels()), namespace.properties());
    }

    @Override
    public void dropNamespace(IcebergNamespace.Name namespace) {
        catalog.dropNamespace(Namespace.of(namespace.levels()));
    }

    @Override
    public IcebergNamespace loadNamespaceMetadata(IcebergNamespace.Name namespace) {
        var metadata = catalog.loadNamespaceMetadata(Namespace.of(namespace.levels()));
        return new IcebergNamespace(namespace, metadata);
    }

    @Override
    public boolean namespaceExists(IcebergNamespace.Name namespace) {
        return catalog.namespaceExists(Namespace.of(namespace.levels()));
    }

    @Override
    public IcebergNamespace.Listing listNamespaces(IcebergNamespace.Listing.Filter filter) {
        List<Namespace> namespaces;

        if (filter.parent().isEmpty()) {
            namespaces = catalog.listNamespaces();
        } else {
            namespaces = catalog.listNamespaces(Namespace.of(filter.parent().get()));
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
    public IcebergView.Metadata createView(IcebergView.CreateRequest rq) {
        var identifier = TableIdentifier.of(Namespace.of(rq.namespace().levels()), rq.name().value());

        var view = catalog
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
        var identifier = TableIdentifier.of(Namespace.of(namespace.levels()), view.value());
        return catalog.viewExists(identifier);
    }

    @Override
    public IcebergView.Metadata loadView(IcebergView view) {
        var loadedView = asBaseView(catalog.loadView(view.toIceberg()));
        var metadata = loadedView.operations().current();
        return toIcebergViewMetadata(view.namespace(), metadata);
    }

    @Override
    public void renameView(IcebergView from, IcebergView to) {
        catalog.renameView(from.toIceberg(), to.toIceberg());
    }

    @Override
    public IcebergView.Listing listViews(IcebergNamespace.Name namespace, IcebergView.Listing.Filter filter) {
        var namespaceIdentifier = Namespace.of(namespace.levels());

        var views = catalog.listViews(namespaceIdentifier);

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
        catalog.dropView(view.toIceberg());
    }

    @Override
    public IcebergView.Metadata replaceView(IcebergView view, IcebergView.UpdateRequest rq) {
        var identifier = view.toIceberg();
        var loadedView = asBaseView(catalog.loadView(identifier));
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

    private SQLViewRepresentation asSQLViewRepresentation(ViewRepresentation value) {
        return (SQLViewRepresentation) value;
    }

    private BaseView asBaseView(View value) {
        return (BaseView) value;
    }

    private IcebergView.Metadata toIcebergViewMetadata(IcebergNamespace.Name namespace, ViewMetadata metadata) {
        var versions = metadata.versions().stream().map(it ->
                new IcebergView.Metadata.Version(
                        new IcebergView.Metadata.VersionId(it.versionId()),
                        new IcebergView.Metadata.Timestamp(it.timestampMillis()),
                        new IcebergView.Metadata.SchemaId(it.schemaId()),
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
                new IcebergView.Metadata.VersionId(it.versionId()),
                new IcebergView.Metadata.Timestamp(it.timestampMillis())
        )).toList();

        return new IcebergView.Metadata(
                new IcebergView.Uuid(metadata.uuid()),
                new IcebergView.Metadata.FormatVersion(metadata.formatVersion()),
                new IcebergView.Location(metadata.location()),
                new IcebergView.Metadata.VersionId(metadata.currentVersionId()),
                versions,
                versionsHistory,
                metadata.schemas(),
                metadata.properties()
        );
    }
}
