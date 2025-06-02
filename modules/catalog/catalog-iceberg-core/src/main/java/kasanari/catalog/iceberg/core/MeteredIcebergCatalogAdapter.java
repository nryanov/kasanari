package kasanari.catalog.iceberg.core;

import kasanari.catalog.iceberg.core.model.IcebergNamespace;
import kasanari.catalog.iceberg.core.model.IcebergTable;
import kasanari.catalog.iceberg.core.model.IcebergValues;
import kasanari.catalog.iceberg.core.model.IcebergView;
import org.apache.iceberg.catalog.Catalog;

import java.util.List;

public class MeteredIcebergCatalogAdapter implements IcebergCatalogAdapter {
    @Override
    public void createNamespace(IcebergNamespace namespace) {

    }

    @Override
    public void dropNamespace(IcebergNamespace.Name namespace) {

    }

    @Override
    public IcebergNamespace loadNamespaceMetadata(IcebergNamespace.Name namespace) {
        return null;
    }

    @Override
    public boolean namespaceExists(IcebergNamespace.Name namespace) {
        return false;
    }

    @Override
    public IcebergNamespace.Listing listNamespaces(IcebergNamespace.Listing.Filter filter) {
        return null;
    }

    @Override
    public IcebergNamespace updateNamespace(IcebergNamespace.Name namespace, IcebergNamespace.Update rq) {
        return null;
    }

    @Override
    public IcebergView.Metadata createView(IcebergView.CreateRequest createRq) {
        return null;
    }

    @Override
    public boolean viewExists(IcebergNamespace.Name namespace, IcebergView.Name view) {
        return false;
    }

    @Override
    public IcebergView.Metadata loadView(IcebergView view) {
        return null;
    }

    @Override
    public void renameView(IcebergView from, IcebergView to) {

    }

    @Override
    public IcebergView.Listing listViews(IcebergNamespace.Name namespace, IcebergView.Listing.Filter filter) {
        return null;
    }

    @Override
    public void dropView(IcebergView view) {

    }

    @Override
    public IcebergView.Metadata replaceView(IcebergView view, IcebergView.UpdateRequest rq) {
        return null;
    }

    @Override
    public boolean tableExists(IcebergNamespace.Name namespace, IcebergTable.Name name) {
        return false;
    }

    @Override
    public void dropTable(IcebergTable table, boolean purge) {

    }

    @Override
    public IcebergTable.Listing listTables(IcebergNamespace.Name namespace, IcebergTable.Listing.Filter filter) {
        return null;
    }

    @Override
    public IcebergTable.LoadedTable createTable(IcebergTable.CreateRequest rq) {
        return null;
    }

    @Override
    public void renameTable(IcebergTable from, IcebergTable to) {

    }

    @Override
    public IcebergTable.LoadedTable registerTable(IcebergTable table, IcebergValues.Location location) {
        return null;
    }

    @Override
    public IcebergTable.Commit updateTable(IcebergTable table, IcebergTable.UpdateRequest rq) {
        return null;
    }

    @Override
    public IcebergTable.LoadedTable loadTable(IcebergTable table) {
        return null;
    }

    @Override
    public void commitTransaction(List<IcebergTable.Transaction> transactions) {

    }

    @Override
    public Catalog delegate() {
        return null;
    }
}
