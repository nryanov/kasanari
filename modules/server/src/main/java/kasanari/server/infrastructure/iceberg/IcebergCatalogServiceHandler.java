package kasanari.server.infrastructure.iceberg;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.catalog.iceberg.api.IcebergRestCatalogApiService;
import kasanari.core.model.CatalogType;
import kasanari.server.infrastructure.security.CatalogHandlerAuthorization;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.rest.requests.CommitTransactionRequest;
import org.apache.iceberg.rest.requests.CreateNamespaceRequest;
import org.apache.iceberg.rest.requests.CreateTableRequest;
import org.apache.iceberg.rest.requests.CreateViewRequest;
import org.apache.iceberg.rest.requests.RegisterTableRequest;
import org.apache.iceberg.rest.requests.RenameTableRequest;
import org.apache.iceberg.rest.requests.ReportMetricsRequest;
import org.apache.iceberg.rest.requests.UpdateNamespacePropertiesRequest;
import org.apache.iceberg.rest.requests.UpdateTableRequest;

import java.util.HashSet;
import java.util.Optional;

@ApplicationScoped
public class IcebergCatalogServiceHandler implements IcebergRestCatalogApiService {
    private static final CatalogType DOMAIN = CatalogType.ICEBERG;

    private final IcebergCatalogRouter icebergCatalogRouter;
    private final AuthorizationService authorizationService;

    public IcebergCatalogServiceHandler(
            IcebergCatalogRouter icebergCatalogRouter,
            AuthorizationService authorizationService
    ) {
        this.icebergCatalogRouter = icebergCatalogRouter;
        this.authorizationService = authorizationService;
    }

    @Override
    public Response commitTransaction(String catalogName, CommitTransactionRequest rq, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergTransactionCommit);
        if (denied.isPresent()) {
            return denied.get();
        }
        icebergCatalogRouter.getOrThrow(catalogName).commitTransaction(rq.tableChanges());
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response createNamespace(String catalogName, CreateNamespaceRequest rq, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergNamespaceCreate);
        if (denied.isPresent()) {
            return denied.get();
        }
        var ns = icebergCatalogRouter.getOrThrow(catalogName).createNamespace(rq.namespace(), rq.properties());
        return Response.status(Response.Status.OK).entity(ns).build();
    }

    @Override
    public Response createTable(String catalogName, String namespace, CreateTableRequest rq, String xIcebergAccessDelegation, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergTableCreate);
        if (denied.isPresent()) {
            return denied.get();
        }
        var ns = Namespace.of(namespace.split("[.]"));
        var table = icebergCatalogRouter.getOrThrow(catalogName).createTable(ns, rq);
        return Response.status(Response.Status.OK).entity(table).build();
    }

    @Override
    public Response createView(String catalogName, String namespace, CreateViewRequest rq, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergViewCreate);
        if (denied.isPresent()) {
            return denied.get();
        }
        var ns = Namespace.of(namespace.split("[.]"));
        var view = icebergCatalogRouter.getOrThrow(catalogName).createView(ns, rq);
        return Response.status(Response.Status.OK).entity(view).build();
    }

    @Override
    public Response dropNamespace(String catalogName, String namespace, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergNamespaceDrop);
        if (denied.isPresent()) {
            return denied.get();
        }
        var ns = Namespace.of(namespace.split("[.]"));
        icebergCatalogRouter.getOrThrow(catalogName).dropNamespace(ns);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response dropTable(String catalogName, String namespace, String table, Boolean purgeRequested, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergTableDrop);
        if (denied.isPresent()) {
            return denied.get();
        }
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, table);
        icebergCatalogRouter.getOrThrow(catalogName).dropTable(identifier, purgeRequested);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response dropView(String catalogName, String namespace, String view, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergViewDrop);
        if (denied.isPresent()) {
            return denied.get();
        }
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, view);
        icebergCatalogRouter.getOrThrow(catalogName).dropView(identifier);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response listNamespaces(String catalogName, String pageToken, Integer pageSize, String parent, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergNamespaceList);
        if (denied.isPresent()) {
            return denied.get();
        }
        var result = icebergCatalogRouter.getOrThrow(catalogName).listNamespaces(pageToken, pageSize, parent);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response listTables(String catalogName, String namespace, String pageToken, Integer pageSize, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergTableList);
        if (denied.isPresent()) {
            return denied.get();
        }
        var ns = Namespace.of(namespace.split("[.]"));
        var result = icebergCatalogRouter.getOrThrow(catalogName).listTables(ns, pageToken, pageSize);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response listViews(String catalogName, String namespace, String pageToken, Integer pageSize, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergViewList);
        if (denied.isPresent()) {
            return denied.get();
        }
        var ns = Namespace.of(namespace.split("[.]"));
        var result = icebergCatalogRouter.getOrThrow(catalogName).listViews(ns, pageToken, pageSize);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response loadNamespaceMetadata(String catalogName, String namespace, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergNamespaceGet);
        if (denied.isPresent()) {
            return denied.get();
        }
        var ns = Namespace.of(namespace.split("[.]"));
        var result = icebergCatalogRouter.getOrThrow(catalogName).loadNamespaceMetadata(ns);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response loadTable(String catalogName, String namespace, String table, String xIcebergAccessDelegation, String ifNoneMatch, String snapshots, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergTableGet);
        if (denied.isPresent()) {
            return denied.get();
        }
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, table);
        var result = icebergCatalogRouter.getOrThrow(catalogName).loadTable(identifier);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response loadView(String catalogName, String namespace, String view, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergViewGet);
        if (denied.isPresent()) {
            return denied.get();
        }
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, view);
        var result = icebergCatalogRouter.getOrThrow(catalogName).loadView(identifier);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response namespaceExists(String catalogName, String namespace, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergNamespaceExists);
        if (denied.isPresent()) {
            return denied.get();
        }
        var ns = Namespace.of(namespace.split("[.]"));
        var result = icebergCatalogRouter.getOrThrow(catalogName).namespaceExists(ns);

        if (result) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response registerTable(String catalogName, String namespace, RegisterTableRequest rq, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergTableAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, rq.name());
        var result = icebergCatalogRouter.getOrThrow(catalogName).registerTable(identifier, rq.metadataLocation());
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response renameTable(String catalogName, RenameTableRequest rq, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergTableAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        icebergCatalogRouter.getOrThrow(catalogName).renameTable(rq.source(), rq.destination());
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response renameView(String catalogName, RenameTableRequest rq, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergViewAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        icebergCatalogRouter.getOrThrow(catalogName).renameView(rq.source(), rq.destination());
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response replaceView(String catalogName, String namespace, String view, UpdateTableRequest rq, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergViewAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, view);
        var result = icebergCatalogRouter.getOrThrow(catalogName).replaceView(identifier, rq);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response tableExists(String catalogName, String namespace, String table, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergTableExists);
        if (denied.isPresent()) {
            return denied.get();
        }
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
        var denied = deny(securityContext, Permission.IcebergNamespaceAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        var ns = Namespace.of(namespace.split("[.]"));
        var result = icebergCatalogRouter.getOrThrow(catalogName).updateNamespace(ns, rq.updates(), new HashSet<>(rq.removals()));
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response updateTable(String catalogName, String namespace, String table, UpdateTableRequest rq, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergTableAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, table);
        var result = icebergCatalogRouter.getOrThrow(catalogName).updateTable(identifier, rq);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @Override
    public Response viewExists(String catalogName, String namespace, String view, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergViewExists);
        if (denied.isPresent()) {
            return denied.get();
        }
        var ns = Namespace.of(namespace.split("[.]"));
        var identifier = TableIdentifier.of(ns, view);
        var result = icebergCatalogRouter.getOrThrow(catalogName).viewExists(identifier);

        if (result) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response reportMetrics(String catalogName, String namespace, String table, ReportMetricsRequest reportMetricsRequest, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.IcebergMetricsReport);
        if (denied.isPresent()) {
            return denied.get();
        }
        icebergCatalogRouter.getOrThrow(catalogName).reportMetrics(namespace, table, reportMetricsRequest);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private Optional<Response> deny(SecurityContext securityContext, Permission permission) {
        return CatalogHandlerAuthorization.denyUnless(authorizationService, securityContext, DOMAIN, permission);
    }
}
