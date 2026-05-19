package kasanari.server.infrastructure.lance;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.lance.api.LanceRestNamespaceService;
import kasanari.server.infrastructure.http.ApiFallbacks;
import org.lance.namespace.model.CreateNamespaceRequest;
import org.lance.namespace.model.DescribeNamespaceRequest;
import org.lance.namespace.model.DropNamespaceRequest;
import org.lance.namespace.model.NamespaceExistsRequest;

@ApplicationScoped
public class LanceNamespaceServiceHandler implements LanceRestNamespaceService {
    private final LanceCatalogRouter lanceCatalogRouter;

    public LanceNamespaceServiceHandler(LanceCatalogRouter lanceCatalogRouter) {
        this.lanceCatalogRouter = lanceCatalogRouter;
    }

    @Override
    public Response createNamespace(String id, CreateNamespaceRequest orgLanceNamespaceModelCreateNamespaceRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceNamespaceService.createNamespace");
    }

    @Override
    public Response describeNamespace(String id, DescribeNamespaceRequest orgLanceNamespaceModelDescribeNamespaceRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceNamespaceService.describeNamespace");
    }

    @Override
    public Response dropNamespace(String id, DropNamespaceRequest orgLanceNamespaceModelDropNamespaceRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceNamespaceService.dropNamespace");
    }

    @Override
    public Response listNamespaces(String id, String delimiter, String pageToken, Integer limit, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceNamespaceService.listNamespaces");
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
        return ApiFallbacks.notImplemented("LanceNamespaceService.listTables");
    }

    @Override
    public Response namespaceExists(String id, NamespaceExistsRequest orgLanceNamespaceModelNamespaceExistsRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceNamespaceService.namespaceExists");
    }
}
