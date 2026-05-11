package kasanari.server.iceberg;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.iceberg.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.api.IcebergRestCatalogApiService;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.rest.requests.CommitTransactionRequest;
import org.apache.iceberg.rest.requests.CreateNamespaceRequest;
import org.apache.iceberg.rest.requests.CreateTableRequest;
import org.apache.iceberg.rest.requests.CreateViewRequest;
import org.apache.iceberg.rest.requests.RegisterTableRequest;
import org.apache.iceberg.rest.requests.RenameTableRequest;
import org.apache.iceberg.rest.requests.UpdateNamespacePropertiesRequest;
import org.apache.iceberg.rest.requests.UpdateTableRequest;

import java.util.HashSet;

@ApplicationScoped
public class IcebergCatalogService implements IcebergRestCatalogApiService {
    private final IcebergCatalogAdapter catalog;

    public IcebergCatalogService(IcebergCatalogAdapter catalog) {
        this.catalog = catalog;
    }

    @Override
    public Response commitTransaction(String prefix, CommitTransactionRequest rq, SecurityContext securityContext) {
        catalog.commitTransaction(rq.tableChanges());
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response createNamespace(String prefix, CreateNamespaceRequest rq, SecurityContext securityContext) {
        var ns = catalog.createNamespace(rq.namespace(), rq.properties());
        return Response.status(Response.Status.OK).entity(ns).build();
    }

    @Override
    public Response createTable(String prefix, String namespace, CreateTableRequest rq, String xIcebergAccessDelegation, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var table = catalog.createTable(ns, rq);
        return Response.status(Response.Status.OK).entity(table).build();
    }

    @Override
    public Response createView(String prefix, String namespace, CreateViewRequest rq, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var view = catalog.createView(ns, rq);
        return Response.status(Response.Status.OK).entity(view).build();
    }

    @Override
    public Response dropNamespace(String prefix, String namespace, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        catalog.dropNamespace(ns);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response dropTable(String prefix, String namespace, String table, Boolean purgeRequested, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, table);
        catalog.dropTable(identifier, purgeRequested);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response dropView(String prefix, String namespace, String view, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, view);
        catalog.dropView(identifier);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response listNamespaces(String prefix, String pageToken, Integer pageSize, String parent, SecurityContext securityContext) {
        var result = catalog.listNamespaces(pageToken, pageSize, parent);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response listTables(String prefix, String namespace, String pageToken, Integer pageSize, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var result = catalog.listTables(ns, pageToken, pageSize);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response listViews(String prefix, String namespace, String pageToken, Integer pageSize, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var result = catalog.listViews(ns, pageToken, pageSize);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response loadNamespaceMetadata(String prefix, String namespace, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var result = catalog.loadNamespaceMetadata(ns);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response loadTable(String prefix, String namespace, String table, String xIcebergAccessDelegation, String ifNoneMatch, String snapshots, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, table);
        var result = catalog.loadTable(identifier);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response loadView(String prefix, String namespace, String view, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, view);
        var result = catalog.loadView(identifier);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response namespaceExists(String prefix, String namespace, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var result = catalog.namespaceExists(ns);

        if (result) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response registerTable(String prefix, String namespace, RegisterTableRequest rq, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, rq.name());
        var result = catalog.registerTable(identifier, rq.metadataLocation());
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response renameTable(String prefix, RenameTableRequest rq, SecurityContext securityContext) {
        catalog.renameTable(rq.source(), rq.destination());
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response renameView(String prefix, RenameTableRequest rq, SecurityContext securityContext) {
        catalog.renameView(rq.source(), rq.destination());
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response replaceView(String prefix, String namespace, String view, UpdateTableRequest rq, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, view);
        var result = catalog.replaceView(identifier, rq);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response tableExists(String prefix, String namespace, String table, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, table);
        var result = catalog.tableExists(identifier);

        if (result) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response updateProperties(String prefix, String namespace, UpdateNamespacePropertiesRequest rq, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var result = catalog.updateNamespace(ns, rq.updates(), new HashSet<>(rq.removals()));
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response updateTable(String prefix, String namespace, String table, UpdateTableRequest rq, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, table);
        var result = catalog.updateTable(identifier, rq);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response viewExists(String prefix, String namespace, String view, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, view);
        var result = catalog.viewExists(identifier);

        if (result) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
