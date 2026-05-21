package kasanari.server.infrastructure.lance;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.lance.api.LanceRestNamespaceService;
import kasanari.authorization.spi.Permission;
import kasanari.instrumentation.spi.lance.LanceCatalogOperation;
import kasanari.server.infrastructure.http.ApiFallbacks;
import kasanari.server.infrastructure.instrumentation.LanceCatalogRequestExecutor;
import org.lance.namespace.model.CreateNamespaceRequest;
import org.lance.namespace.model.DescribeNamespaceRequest;
import org.lance.namespace.model.DropNamespaceRequest;
import org.lance.namespace.model.NamespaceExistsRequest;

import java.util.Map;

@ApplicationScoped
public class LanceNamespaceServiceHandler implements LanceRestNamespaceService {
    private final LanceCatalogRequestExecutor executor;

    public LanceNamespaceServiceHandler(LanceCatalogRequestExecutor executor) {
        this.executor = executor;
    }

    @Override
    public Response createNamespace(String id, CreateNamespaceRequest orgLanceNamespaceModelCreateNamespaceRequest, String delimiter, SecurityContext securityContext) {
        return executor.execute(securityContext, id, LanceCatalogOperation.CREATE_NAMESPACE, Permission.LanceNamespaceCreate, Map.of(), () ->
                ApiFallbacks.notImplemented("LanceNamespaceService.createNamespace"));
    }

    @Override
    public Response describeNamespace(String id, DescribeNamespaceRequest orgLanceNamespaceModelDescribeNamespaceRequest, String delimiter, SecurityContext securityContext) {
        return executor.execute(securityContext, id, LanceCatalogOperation.DESCRIBE_NAMESPACE, Permission.LanceNamespaceGet, Map.of(), () ->
                ApiFallbacks.notImplemented("LanceNamespaceService.describeNamespace"));
    }

    @Override
    public Response dropNamespace(String id, DropNamespaceRequest orgLanceNamespaceModelDropNamespaceRequest, String delimiter, SecurityContext securityContext) {
        return executor.execute(securityContext, id, LanceCatalogOperation.DROP_NAMESPACE, Permission.LanceNamespaceDrop, Map.of(), () ->
                ApiFallbacks.notImplemented("LanceNamespaceService.dropNamespace"));
    }

    @Override
    public Response listNamespaces(String id, String delimiter, String pageToken, Integer limit, SecurityContext securityContext) {
        return executor.execute(securityContext, id, LanceCatalogOperation.LIST_NAMESPACES, Permission.LanceNamespaceList, Map.of(), () ->
                ApiFallbacks.notImplemented("LanceNamespaceService.listNamespaces"));
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
        return executor.execute(securityContext, id, LanceCatalogOperation.LIST_TABLES, Permission.LanceTableList, Map.of(), () ->
                ApiFallbacks.notImplemented("LanceNamespaceService.listTables"));
    }

    @Override
    public Response namespaceExists(String id, NamespaceExistsRequest orgLanceNamespaceModelNamespaceExistsRequest, String delimiter, SecurityContext securityContext) {
        return executor.execute(securityContext, id, LanceCatalogOperation.NAMESPACE_EXISTS, Permission.LanceNamespaceExists, Map.of(), () ->
                ApiFallbacks.notImplemented("LanceNamespaceService.namespaceExists"));
    }
}
