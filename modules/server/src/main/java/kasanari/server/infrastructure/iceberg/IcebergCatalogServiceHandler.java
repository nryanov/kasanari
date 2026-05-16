package kasanari.server.infrastructure.iceberg;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
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
public class IcebergCatalogServiceHandler implements IcebergRestCatalogApiService {
    private final IcebergCatalogRouter icebergCatalogRouter;

    public IcebergCatalogServiceHandler(IcebergCatalogRouter icebergCatalogRouter) {
        this.icebergCatalogRouter = icebergCatalogRouter;
    }

    @Override
    public Response commitTransaction(String catalogName, CommitTransactionRequest rq, SecurityContext securityContext) {
        icebergCatalogRouter.getOrThrow(catalogName).commitTransaction(rq.tableChanges());
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response createNamespace(String catalogName, CreateNamespaceRequest rq, SecurityContext securityContext) {
        var ns = icebergCatalogRouter.getOrThrow(catalogName).createNamespace(rq.namespace(), rq.properties());
        return Response.status(Response.Status.OK).entity(ns).build();
    }

    @Override
    public Response createTable(String catalogName, String namespace, CreateTableRequest rq, String xIcebergAccessDelegation, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var table = icebergCatalogRouter.getOrThrow(catalogName).createTable(ns, rq);
        return Response.status(Response.Status.OK).entity(table).build();
    }

    @Override
    public Response createView(String catalogName, String namespace, CreateViewRequest rq, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var view = icebergCatalogRouter.getOrThrow(catalogName).createView(ns, rq);
        return Response.status(Response.Status.OK).entity(view).build();
    }

    @Override
    public Response dropNamespace(String catalogName, String namespace, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        icebergCatalogRouter.getOrThrow(catalogName).dropNamespace(ns);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response dropTable(String catalogName, String namespace, String table, Boolean purgeRequested, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, table);
        icebergCatalogRouter.getOrThrow(catalogName).dropTable(identifier, purgeRequested);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response dropView(String catalogName, String namespace, String view, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, view);
        icebergCatalogRouter.getOrThrow(catalogName).dropView(identifier);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response listNamespaces(String catalogName, String pageToken, Integer pageSize, String parent, SecurityContext securityContext) {
        var result = icebergCatalogRouter.getOrThrow(catalogName).listNamespaces(pageToken, pageSize, parent);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response listTables(String catalogName, String namespace, String pageToken, Integer pageSize, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var result = icebergCatalogRouter.getOrThrow(catalogName).listTables(ns, pageToken, pageSize);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response listViews(String catalogName, String namespace, String pageToken, Integer pageSize, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var result = icebergCatalogRouter.getOrThrow(catalogName).listViews(ns, pageToken, pageSize);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response loadNamespaceMetadata(String catalogName, String namespace, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var result = icebergCatalogRouter.getOrThrow(catalogName).loadNamespaceMetadata(ns);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response loadTable(String catalogName, String namespace, String table, String xIcebergAccessDelegation, String ifNoneMatch, String snapshots, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, table);
        var result = icebergCatalogRouter.getOrThrow(catalogName).loadTable(identifier);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response loadView(String catalogName, String namespace, String view, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, view);
        var result = icebergCatalogRouter.getOrThrow(catalogName).loadView(identifier);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response namespaceExists(String catalogName, String namespace, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var result = icebergCatalogRouter.getOrThrow(catalogName).namespaceExists(ns);

        if (result) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response registerTable(String catalogName, String namespace, RegisterTableRequest rq, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, rq.name());
        var result = icebergCatalogRouter.getOrThrow(catalogName).registerTable(identifier, rq.metadataLocation());
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response renameTable(String catalogName, RenameTableRequest rq, SecurityContext securityContext) {
        icebergCatalogRouter.getOrThrow(catalogName).renameTable(rq.source(), rq.destination());
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response renameView(String catalogName, RenameTableRequest rq, SecurityContext securityContext) {
        icebergCatalogRouter.getOrThrow(catalogName).renameView(rq.source(), rq.destination());
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response replaceView(String catalogName, String namespace, String view, UpdateTableRequest rq, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, view);
        var result = icebergCatalogRouter.getOrThrow(catalogName).replaceView(identifier, rq);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response tableExists(String catalogName, String namespace, String table, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, table);
        var result = icebergCatalogRouter.getOrThrow(catalogName).tableExists(identifier);

        if (result) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response updateProperties(String catalogName, String namespace, UpdateNamespacePropertiesRequest rq, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var result = icebergCatalogRouter.getOrThrow(catalogName).updateNamespace(ns, rq.updates(), new HashSet<>(rq.removals()));
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response updateTable(String catalogName, String namespace, String table, UpdateTableRequest rq, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, table);
        var result = icebergCatalogRouter.getOrThrow(catalogName).updateTable(identifier, rq);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response viewExists(String catalogName, String namespace, String view, SecurityContext securityContext) {
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, view);
        var result = icebergCatalogRouter.getOrThrow(catalogName).viewExists(identifier);

        if (result) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
