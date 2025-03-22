package kasanari.catalog.iceberg.core;

import kasanari.catalog.iceberg.core.model.IcebergNamespace;
import kasanari.catalog.iceberg.core.model.IcebergNamespaceListing;

public interface IcebergCatalogAdapter {
    void createNamespace(IcebergNamespace namespace);

    void dropNamespace(IcebergNamespace.Name namespace);

     IcebergNamespace loadNamespaceMetadata(IcebergNamespace.Name namespace);

    boolean namespaceExists(IcebergNamespace.Name namespace);

    IcebergNamespaceListing listNamespaces(IcebergNamespaceListing.Filter filter);
}
