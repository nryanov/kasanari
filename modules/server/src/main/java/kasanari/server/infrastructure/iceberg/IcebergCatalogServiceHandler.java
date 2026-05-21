package kasanari.server.infrastructure.iceberg;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.spi.Permission;
import kasanari.catalog.iceberg.api.IcebergRestCatalogApiService;
import kasanari.instrumentation.spi.iceberg.IcebergCatalogOperation;
import kasanari.server.infrastructure.instrumentation.IcebergCatalogRequestExecutor;
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
import java.util.Map;

@ApplicationScoped
public class IcebergCatalogServiceHandler implements IcebergRestCatalogApiService {
    private final IcebergCatalogRouter icebergCatalogRouter;
    private final IcebergCatalogRequestExecutor executor;

    public IcebergCatalogServiceHandler(
            IcebergCatalogRouter icebergCatalogRouter,
            IcebergCatalogRequestExecutor executor
    ) {
        this.icebergCatalogRouter = icebergCatalogRouter;
        this.executor = executor;
    }

    @Override
    public Response commitTransaction(String catalogName, CommitTransactionRequest rq, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.COMMIT_TRANSACTION, Permission.IcebergTransactionCommit, Map.of(), () -> {
            icebergCatalogRouter.getOrThrow(catalogName).commitTransaction(rq.tableChanges());
            return Response.status(Response.Status.NO_CONTENT).build();
        });
    }

    @Override
    public Response createNamespace(String catalogName, CreateNamespaceRequest rq, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.CREATE_NAMESPACE, Permission.IcebergNamespaceCreate, Map.of("namespace", rq.namespace().toString()),
                () -> {
                    var ns = icebergCatalogRouter.getOrThrow(catalogName).createNamespace(rq.namespace(), rq.properties());
                    return Response.status(Response.Status.OK).entity(ns).build();
                }
        );
    }

    @Override
    public Response createTable(String catalogName, String namespace, CreateTableRequest rq, String xIcebergAccessDelegation, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.CREATE_TABLE, Permission.IcebergTableCreate, Map.of("namespace", namespace, "table", rq.name()),
                () -> {
                    var ns = Namespace.of(namespace.split("[.]"));
                    var table = icebergCatalogRouter.getOrThrow(catalogName).createTable(ns, rq);
                    return Response.status(Response.Status.OK).entity(table).build();
                }
        );
    }

    @Override
    public Response createView(String catalogName, String namespace, CreateViewRequest rq, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.CREATE_VIEW, Permission.IcebergViewCreate, Map.of("namespace", namespace, "view", rq.name()),
                () -> {
                    var ns = Namespace.of(namespace.split("[.]"));
                    var view = icebergCatalogRouter.getOrThrow(catalogName).createView(ns, rq);
                    return Response.status(Response.Status.OK).entity(view).build();
                }
        );
    }

    @Override
    public Response dropNamespace(String catalogName, String namespace, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.DROP_NAMESPACE, Permission.IcebergNamespaceDrop, Map.of("namespace", namespace),
                () -> {
                    var ns = Namespace.of(namespace.split("[.]"));
                    icebergCatalogRouter.getOrThrow(catalogName).dropNamespace(ns);
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
        );
    }

    @Override
    public Response dropTable(String catalogName, String namespace, String table, Boolean purgeRequested, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.DROP_TABLE, Permission.IcebergTableDrop, Map.of("namespace", namespace, "table", table, "purge", String.valueOf(purgeRequested)),
                () -> {
                    var ns = Namespace.of(namespace.split("[.]"));
                    var identifier = TableIdentifier.of(ns, table);
                    icebergCatalogRouter.getOrThrow(catalogName).dropTable(identifier, purgeRequested);
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
        );
    }

    @Override
    public Response dropView(String catalogName, String namespace, String view, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.DROP_VIEW, Permission.IcebergViewDrop, Map.of("namespace", namespace, "view", view),
                () -> {
                    var ns = Namespace.of(namespace.split("[.]"));
                    var identifier = TableIdentifier.of(ns, view);
                    icebergCatalogRouter.getOrThrow(catalogName).dropView(identifier);
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
        );
    }

    @Override
    public Response listNamespaces(String catalogName, String pageToken, Integer pageSize, String parent, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.LIST_NAMESPACES, Permission.IcebergNamespaceList, Map.of("parent", String.valueOf(parent)),
                () -> {
                    var result = icebergCatalogRouter.getOrThrow(catalogName).listNamespaces(pageToken, pageSize, parent);
                    return Response.status(Response.Status.OK).entity(result).build();
                }
        );
    }

    @Override
    public Response listTables(String catalogName, String namespace, String pageToken, Integer pageSize, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.LIST_TABLES, Permission.IcebergTableList, Map.of("namespace", namespace),
                () -> {
                    var ns = Namespace.of(namespace.split("[.]"));
                    var result = icebergCatalogRouter.getOrThrow(catalogName).listTables(ns, pageToken, pageSize);
                    return Response.status(Response.Status.OK).entity(result).build();
                }
        );
    }

    @Override
    public Response listViews(String catalogName, String namespace, String pageToken, Integer pageSize, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.LIST_VIEWS, Permission.IcebergViewList, Map.of("namespace", namespace),
                () -> {
                    var ns = Namespace.of(namespace.split("[.]"));
                    var result = icebergCatalogRouter.getOrThrow(catalogName).listViews(ns, pageToken, pageSize);
                    return Response.status(Response.Status.OK).entity(result).build();
                }
        );
    }

    @Override
    public Response loadNamespaceMetadata(String catalogName, String namespace, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.LOAD_NAMESPACE, Permission.IcebergNamespaceGet, Map.of("namespace", namespace),
                () -> {
                    var ns = Namespace.of(namespace.split("[.]"));
                    var result = icebergCatalogRouter.getOrThrow(catalogName).loadNamespaceMetadata(ns);
                    return Response.status(Response.Status.OK).entity(result).build();
                }
        );
    }

    @Override
    public Response loadTable(String catalogName, String namespace, String table, String xIcebergAccessDelegation, String ifNoneMatch, String snapshots, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.LOAD_TABLE, Permission.IcebergTableGet, Map.of("namespace", namespace, "table", table),
                () -> {
                    var ns = Namespace.of(namespace.split("[.]"));
                    var identifier = TableIdentifier.of(ns, table);
                    var result = icebergCatalogRouter.getOrThrow(catalogName).loadTable(identifier);
                    return Response.status(Response.Status.OK).entity(result).build();
                }
        );
    }

    @Override
    public Response loadView(String catalogName, String namespace, String view, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.LOAD_VIEW, Permission.IcebergViewGet, Map.of("namespace", namespace, "view", view),
                () -> {
                    var ns = Namespace.of(namespace.split("[.]"));
                    var identifier = TableIdentifier.of(ns, view);
                    var result = icebergCatalogRouter.getOrThrow(catalogName).loadView(identifier);
                    return Response.status(Response.Status.OK).entity(result).build();
                }
        );
    }

    @Override
    public Response namespaceExists(String catalogName, String namespace, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.NAMESPACE_EXISTS, Permission.IcebergNamespaceExists, Map.of("namespace", namespace),
                () -> {
                    var ns = Namespace.of(namespace.split("[.]"));
                    var result = icebergCatalogRouter.getOrThrow(catalogName).namespaceExists(ns);
                    if (result) {
                        return Response.status(Response.Status.NO_CONTENT).build();
                    }
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
        );
    }

    @Override
    public Response registerTable(String catalogName, String namespace, RegisterTableRequest rq, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.REGISTER_TABLE, Permission.IcebergTableAlter, Map.of("namespace", namespace, "table", rq.name()),
                () -> {
                    var ns = Namespace.of(namespace.split("[.]"));
                    var identifier = TableIdentifier.of(ns, rq.name());
                    var result = icebergCatalogRouter.getOrThrow(catalogName).registerTable(identifier, rq.metadataLocation());
                    return Response.status(Response.Status.OK).entity(result).build();
                }
        );
    }

    @Override
    public Response renameTable(String catalogName, RenameTableRequest rq, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.RENAME_TABLE, Permission.IcebergTableAlter, Map.of("from", rq.source().toString(), "to", rq.destination().toString()),
                () -> {
                    icebergCatalogRouter.getOrThrow(catalogName).renameTable(rq.source(), rq.destination());
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
        );
    }

    @Override
    public Response renameView(String catalogName, RenameTableRequest rq, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.RENAME_VIEW, Permission.IcebergViewAlter, Map.of("from", rq.source().toString(), "to", rq.destination().toString()),
                () -> {
                    icebergCatalogRouter.getOrThrow(catalogName).renameView(rq.source(), rq.destination());
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
        );
    }

    @Override
    public Response replaceView(String catalogName, String namespace, String view, UpdateTableRequest rq, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.REPLACE_VIEW, Permission.IcebergViewAlter, Map.of("namespace", namespace, "view", view),
                () -> {
                    var ns = Namespace.of(namespace.split("[.]"));
                    var identifier = TableIdentifier.of(ns, view);
                    var result = icebergCatalogRouter.getOrThrow(catalogName).replaceView(identifier, rq);
                    return Response.status(Response.Status.OK).entity(result).build();
                }
        );
    }

    @Override
    public Response tableExists(String catalogName, String namespace, String table, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.TABLE_EXISTS, Permission.IcebergTableExists, Map.of("namespace", namespace, "table", table),
                () -> {
                    var ns = Namespace.of(namespace.split("[.]"));
                    var identifier = TableIdentifier.of(ns, table);
                    var result = icebergCatalogRouter.getOrThrow(catalogName).tableExists(identifier);
                    if (result) {
                        return Response.status(Response.Status.NO_CONTENT).build();
                    }
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
        );
    }

    @Override
    public Response updateProperties(String catalogName, String namespace, UpdateNamespacePropertiesRequest rq, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.UPDATE_NAMESPACE, Permission.IcebergNamespaceAlter, Map.of("namespace", namespace),
                () -> {
                    var ns = Namespace.of(namespace.split("[.]"));
                    var result = icebergCatalogRouter.getOrThrow(catalogName).updateNamespace(ns, rq.updates(), new HashSet<>(rq.removals()));
                    return Response.status(Response.Status.OK).entity(result).build();
                }
        );
    }

    @Override
    public Response updateTable(String catalogName, String namespace, String table, UpdateTableRequest rq, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.UPDATE_TABLE, Permission.IcebergTableAlter, Map.of("namespace", namespace, "table", table),
                () -> {
                    var ns = Namespace.of(namespace.split("[.]"));
                    var identifier = TableIdentifier.of(ns, table);
                    var result = icebergCatalogRouter.getOrThrow(catalogName).updateTable(identifier, rq);
                    return Response.status(Response.Status.OK).entity(result).build();
                }
        );
    }

    @Override
    public Response viewExists(String catalogName, String namespace, String view, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.VIEW_EXISTS, Permission.IcebergViewExists, Map.of("namespace", namespace, "view", view),
                () -> {
                    var ns = Namespace.of(namespace.split("[.]"));
                    var identifier = TableIdentifier.of(ns, view);
                    var result = icebergCatalogRouter.getOrThrow(catalogName).viewExists(identifier);
                    if (result) {
                        return Response.status(Response.Status.NO_CONTENT).build();
                    }
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
        );
    }

    @Override
    public Response reportMetrics(String catalogName, String namespace, String table, ReportMetricsRequest reportMetricsRequest, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, IcebergCatalogOperation.REPORT_METRICS, Permission.IcebergMetricsReport, Map.of("namespace", namespace, "table", table),
                () -> {
                    icebergCatalogRouter.getOrThrow(catalogName).reportMetrics(namespace, table, reportMetricsRequest);
                    return Response.status(Response.Status.NO_CONTENT).build();
                }
        );
    }
}
