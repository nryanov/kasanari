package kasanari.catalog.iceberg.core;

import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.rest.requests.CreateTableRequest;
import org.apache.iceberg.rest.requests.CreateViewRequest;
import org.apache.iceberg.rest.requests.UpdateTableRequest;
import org.apache.iceberg.rest.responses.CreateNamespaceResponse;
import org.apache.iceberg.rest.responses.GetNamespaceResponse;
import org.apache.iceberg.rest.responses.ListNamespacesResponse;
import org.apache.iceberg.rest.responses.ListTablesResponse;
import org.apache.iceberg.rest.responses.LoadTableResponse;
import org.apache.iceberg.rest.responses.LoadViewResponse;
import org.apache.iceberg.rest.responses.UpdateNamespacePropertiesResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MeteredIcebergCatalogAdapter implements IcebergCatalogAdapter {
    @Override
    public CreateNamespaceResponse createNamespace(Namespace namespace, Map<String, String> properties) {
        return null;
    }

    @Override
    public void dropNamespace(Namespace namespace) {

    }

    @Override
    public GetNamespaceResponse loadNamespaceMetadata(Namespace namespace) {
        return null;
    }

    @Override
    public boolean namespaceExists(Namespace namespace) {
        return false;
    }

    @Override
    public ListNamespacesResponse listNamespaces(String pageToken, Integer pageSize, String parent) {
        return null;
    }

    @Override
    public UpdateNamespacePropertiesResponse updateNamespace(Namespace namespace, Map<String, String> updates, Set<String> removals) {
        return null;
    }

    @Override
    public LoadViewResponse createView(Namespace namespace, CreateViewRequest rq) {
        return null;
    }

    @Override
    public boolean viewExists(TableIdentifier view) {
        return false;
    }

    @Override
    public LoadViewResponse loadView(TableIdentifier view) {
        return null;
    }

    @Override
    public void renameView(TableIdentifier from, TableIdentifier to) {

    }

    @Override
    public ListTablesResponse listViews(Namespace namespace, String pageToken, Integer pageSize) {
        return null;
    }

    @Override
    public void dropView(TableIdentifier view) {

    }

    @Override
    public LoadViewResponse replaceView(TableIdentifier view, UpdateTableRequest rq) {
        return null;
    }

    @Override
    public boolean tableExists(TableIdentifier table) {
        return false;
    }

    @Override
    public void dropTable(TableIdentifier table, boolean purge) {

    }

    @Override
    public ListTablesResponse listTables(Namespace namespace, String pageToken, Integer pageSize) {
        return null;
    }

    @Override
    public LoadTableResponse createTable(Namespace namespace, CreateTableRequest rq) {
        return null;
    }

    @Override
    public void renameTable(TableIdentifier from, TableIdentifier to) {

    }

    @Override
    public LoadTableResponse registerTable(TableIdentifier table, String location) {
        return null;
    }

    @Override
    public LoadTableResponse updateTable(TableIdentifier table, UpdateTableRequest rq) {
        return null;
    }

    @Override
    public LoadTableResponse loadTable(TableIdentifier table) {
        return null;
    }

    @Override
    public void commitTransaction(List<UpdateTableRequest> transactions) {

    }

    @Override
    public Catalog delegate() {
        return null;
    }
}
