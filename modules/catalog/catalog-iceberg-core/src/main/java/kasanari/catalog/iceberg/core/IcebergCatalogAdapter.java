package kasanari.catalog.iceberg.core;

import kasanari.catalog.iceberg.core.model.IcebergNamespace;
import kasanari.catalog.iceberg.core.model.IcebergView;

public interface IcebergCatalogAdapter {
    void createNamespace(IcebergNamespace namespace);

    void dropNamespace(IcebergNamespace.Name namespace);

     IcebergNamespace loadNamespaceMetadata(IcebergNamespace.Name namespace);

    boolean namespaceExists(IcebergNamespace.Name namespace);

    IcebergNamespace.Listing listNamespaces(IcebergNamespace.Listing.Filter filter);

    IcebergView.Metadata createView(IcebergView.CreateRequest createRq);

    boolean viewExists(IcebergNamespace.Name namespace, IcebergView.Name view);

    IcebergView.Metadata loadView(IcebergView view);

    void renameView(IcebergView from, IcebergView to);

    IcebergView.Listing listViews(IcebergNamespace.Name namespace, IcebergView.Listing.Filter filter);

    void dropView(IcebergView view);

    IcebergView.Metadata replaceView(IcebergView view, IcebergView.UpdateRequest rq);
}
