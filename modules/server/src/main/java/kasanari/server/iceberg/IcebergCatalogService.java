package kasanari.server.iceberg;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.iceberg.api.IcebergRestCatalogApiService;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import org.apache.iceberg.rest.requests.CommitTransactionRequest;
import org.apache.iceberg.rest.requests.CreateNamespaceRequest;
import org.apache.iceberg.rest.requests.CreateTableRequest;
import org.apache.iceberg.rest.requests.CreateViewRequest;
import org.apache.iceberg.rest.requests.RegisterTableRequest;
import org.apache.iceberg.rest.requests.RenameTableRequest;
import org.apache.iceberg.rest.requests.UpdateNamespacePropertiesRequest;
import org.apache.iceberg.rest.requests.UpdateTableRequest;

@ApplicationScoped
public class IcebergCatalogService implements IcebergRestCatalogApiService {
    private final IcebergCatalogAdapter catalog;

    public IcebergCatalogService(IcebergCatalogAdapter catalog) {
        this.catalog = catalog;
    }

    @Override
    public Response commitTransaction(String prefix, CommitTransactionRequest commitTransactionRequest, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.commitTransaction(prefix, commitTransactionRequest, securityContext);
    }

    @Override
    public Response createNamespace(String prefix, CreateNamespaceRequest createNamespaceRequest, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.createNamespace(prefix, createNamespaceRequest, securityContext);
    }

    @Override
    public Response createTable(String prefix, String namespace, CreateTableRequest createTableRequest, String xIcebergAccessDelegation, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.createTable(prefix, namespace, createTableRequest, xIcebergAccessDelegation, securityContext);
    }

    @Override
    public Response createView(String prefix, String namespace, CreateViewRequest createViewRequest, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.createView(prefix, namespace, createViewRequest, securityContext);
    }

    @Override
    public Response dropNamespace(String prefix, String namespace, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.dropNamespace(prefix, namespace, securityContext);
    }

    @Override
    public Response dropTable(String prefix, String namespace, String table, Boolean purgeRequested, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.dropTable(prefix, namespace, table, purgeRequested, securityContext);
    }

    @Override
    public Response dropView(String prefix, String namespace, String view, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.dropView(prefix, namespace, view, securityContext);
    }

    @Override
    public Response listNamespaces(String prefix, String pageToken, Integer pageSize, String parent, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.listNamespaces(prefix, pageToken, pageSize, parent, securityContext);
    }

    @Override
    public Response listTables(String prefix, String namespace, String pageToken, Integer pageSize, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.listTables(prefix, namespace, pageToken, pageSize, securityContext);
    }

    @Override
    public Response listViews(String prefix, String namespace, String pageToken, Integer pageSize, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.listViews(prefix, namespace, pageToken, pageSize, securityContext);
    }

    @Override
    public Response loadNamespaceMetadata(String prefix, String namespace, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.loadNamespaceMetadata(prefix, namespace, securityContext);
    }

    @Override
    public Response loadTable(String prefix, String namespace, String table, String xIcebergAccessDelegation, String ifNoneMatch, String snapshots, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.loadTable(prefix, namespace, table, xIcebergAccessDelegation, ifNoneMatch, snapshots, securityContext);
    }

    @Override
    public Response loadView(String prefix, String namespace, String view, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.loadView(prefix, namespace, view, securityContext);
    }

    @Override
    public Response namespaceExists(String prefix, String namespace, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.namespaceExists(prefix, namespace, securityContext);
    }

    @Override
    public Response registerTable(String prefix, String namespace, RegisterTableRequest registerTableRequest, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.registerTable(prefix, namespace, registerTableRequest, securityContext);
    }

    @Override
    public Response renameTable(String prefix, RenameTableRequest renameTableRequest, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.renameTable(prefix, renameTableRequest, securityContext);
    }

    @Override
    public Response renameView(String prefix, RenameTableRequest renameTableRequest, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.renameView(prefix, renameTableRequest, securityContext);
    }

    @Override
    public Response replaceView(String prefix, String namespace, String view, UpdateTableRequest orgApacheIcebergRestRequestsUpdateTableRequest, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.replaceView(prefix, namespace, view, orgApacheIcebergRestRequestsUpdateTableRequest, securityContext);
    }

    @Override
    public Response tableExists(String prefix, String namespace, String table, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.tableExists(prefix, namespace, table, securityContext);
    }

    @Override
    public Response updateProperties(String prefix, String namespace, UpdateNamespacePropertiesRequest updateNamespacePropertiesRequest, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.updateProperties(prefix, namespace, updateNamespacePropertiesRequest, securityContext);
    }

    @Override
    public Response updateTable(String prefix, String namespace, String table, UpdateTableRequest orgApacheIcebergRestRequestsUpdateTableRequest, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.updateTable(prefix, namespace, table, orgApacheIcebergRestRequestsUpdateTableRequest, securityContext);
    }

    @Override
    public Response viewExists(String prefix, String namespace, String view, SecurityContext securityContext) {
        return IcebergRestCatalogApiService.super.viewExists(prefix, namespace, view, securityContext);
    }
}
