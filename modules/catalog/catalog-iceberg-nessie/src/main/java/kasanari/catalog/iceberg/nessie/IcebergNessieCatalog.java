package kasanari.catalog.iceberg.nessie;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.model.IcebergNamespace;
import kasanari.catalog.iceberg.core.model.IcebergNamespaceListing;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.nessie.NessieCatalog;

import java.util.List;
import java.util.Optional;


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
    public IcebergNamespaceListing listNamespaces(IcebergNamespaceListing.Filter filter) {
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

        return new IcebergNamespaceListing(subList, nextToken);
    }
}
