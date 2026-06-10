package kasanari.server.infrastructure.lance;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.lance.api.LanceRestNamespaceService;
import kasanari.authorization.spi.Permission;
import kasanari.instrumentation.spi.lance.LanceCatalogOperation;
import kasanari.server.infrastructure.instrumentation.LanceCatalogRequestExecutor;
import org.lance.namespace.model.CreateNamespaceRequest;
import org.lance.namespace.model.DescribeNamespaceRequest;
import org.lance.namespace.model.DropNamespaceRequest;
import org.lance.namespace.model.ListNamespacesRequest;
import org.lance.namespace.model.ListTablesRequest;
import org.lance.namespace.model.NamespaceExistsRequest;

import java.util.List;
import java.util.Map;

import static kasanari.server.infrastructure.lance.LanceCatalogHelper.parseCatalogNamespace;

@ApplicationScoped
public class LanceNamespaceServiceHandler implements LanceRestNamespaceService {
    private final LanceCatalogRequestExecutor executor;
    private final LanceCatalogRouter catalogRouter;

    public LanceNamespaceServiceHandler(LanceCatalogRequestExecutor executor, LanceCatalogRouter catalogRouter) {
        this.executor = executor;
        this.catalogRouter = catalogRouter;
    }

    @Override
    public Response createNamespace(String id, CreateNamespaceRequest orgLanceNamespaceModelCreateNamespaceRequest, String delimiter, SecurityContext securityContext) {
        var parsedId = parseCatalogNamespace(id, delimiter);
        orgLanceNamespaceModelCreateNamespaceRequest.id(List.of(parsedId.namespace()));

        return executor.execute(
                securityContext,
                parsedId.catalog(),
                LanceCatalogOperation.CREATE_NAMESPACE,
                Permission.LanceNamespaceCreate,
                Map.of("namespace", parsedId.namespace()),
                () -> Response.ok(catalogRouter.getOrThrow(parsedId.catalog()).createNamespace(orgLanceNamespaceModelCreateNamespaceRequest)).build()
        );
    }

    @Override
    public Response describeNamespace(String id, DescribeNamespaceRequest orgLanceNamespaceModelDescribeNamespaceRequest, String delimiter, SecurityContext securityContext) {
        var parsedId = parseCatalogNamespace(id, delimiter);
        orgLanceNamespaceModelDescribeNamespaceRequest.id(List.of(parsedId.namespace()));

        return executor.execute(
                securityContext,
                parsedId.catalog(),
                LanceCatalogOperation.DESCRIBE_NAMESPACE,
                Permission.LanceNamespaceGet,
                Map.of("namespace", parsedId.namespace()),
                () -> Response.ok(catalogRouter.getOrThrow(parsedId.catalog()).describeNamespace(orgLanceNamespaceModelDescribeNamespaceRequest)).build()
        );
    }

    @Override
    public Response dropNamespace(String id, DropNamespaceRequest orgLanceNamespaceModelDropNamespaceRequest, String delimiter, SecurityContext securityContext) {
        var parsedId = parseCatalogNamespace(id, delimiter);
        orgLanceNamespaceModelDropNamespaceRequest.id(List.of(parsedId.namespace()));

        return executor.execute(
                securityContext,
                parsedId.catalog(),
                LanceCatalogOperation.DROP_NAMESPACE,
                Permission.LanceNamespaceDrop,
                Map.of("namespace", parsedId.namespace()),
                () -> Response.ok(catalogRouter.getOrThrow(parsedId.catalog()).dropNamespace(orgLanceNamespaceModelDropNamespaceRequest)).build()
        );
    }

    @Override
    public Response listNamespaces(String id, String delimiter, String pageToken, Integer limit, SecurityContext securityContext) {
        var parsedId = parseCatalogNamespace(id, delimiter);
        var request = new ListNamespacesRequest()
                .id(List.of(parsedId.namespace()))
                .pageToken(pageToken)
                .limit(limit);

        return executor.execute(
                securityContext,
                parsedId.catalog(),
                LanceCatalogOperation.LIST_NAMESPACES,
                Permission.LanceNamespaceList,
                Map.of("namespace", parsedId.namespace()),
                () -> Response.ok(catalogRouter.getOrThrow(parsedId.catalog()).listNamespaces(request)).build()
        );
    }

    @Override
    public Response listTables(
            String id,
            String delimiter,
            String pageToken,
            Integer limit,
            Boolean includeDeclared,
            SecurityContext securityContext
    ) {
        var parsedId = parseCatalogNamespace(id, delimiter);
        var request = new ListTablesRequest()
                .id(List.of(parsedId.namespace()))
                .pageToken(pageToken)
                .limit(limit)
                .includeDeclared(includeDeclared);

        return executor.execute(
                securityContext,
                parsedId.catalog(),
                LanceCatalogOperation.LIST_TABLES,
                Permission.LanceTableList,
                Map.of("namespace", parsedId.namespace()),
                () -> Response.ok(catalogRouter.getOrThrow(parsedId.catalog()).listTables(request)).build()
        );
    }

    @Override
    public Response namespaceExists(String id, NamespaceExistsRequest orgLanceNamespaceModelNamespaceExistsRequest, String delimiter, SecurityContext securityContext) {
        var parsedId = parseCatalogNamespace(id, delimiter);
        orgLanceNamespaceModelNamespaceExistsRequest.id(List.of(parsedId.namespace()));

        return executor.execute(
                securityContext,
                parsedId.catalog(),
                LanceCatalogOperation.NAMESPACE_EXISTS,
                Permission.LanceNamespaceExists,
                Map.of("namespace", parsedId.namespace()),
                () -> {
                    catalogRouter.getOrThrow(parsedId.catalog()).namespaceExists(orgLanceNamespaceModelNamespaceExistsRequest);
                    return Response.ok().build();
                }
        );
    }
}
