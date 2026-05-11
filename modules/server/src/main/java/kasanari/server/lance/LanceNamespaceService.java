package kasanari.server.lance;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.lance.api.LanceRestNamespaceService;
import org.lance.namespace.model.CreateNamespaceRequest;
import org.lance.namespace.model.DescribeNamespaceRequest;
import org.lance.namespace.model.DropNamespaceRequest;
import org.lance.namespace.model.NamespaceExistsRequest;

@ApplicationScoped
public class LanceNamespaceService implements LanceRestNamespaceService {
    @Override
    public Response createNamespace(String id, CreateNamespaceRequest orgLanceNamespaceModelCreateNamespaceRequest, String delimiter, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response describeNamespace(String id, DescribeNamespaceRequest orgLanceNamespaceModelDescribeNamespaceRequest, String delimiter, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response dropNamespace(String id, DropNamespaceRequest orgLanceNamespaceModelDropNamespaceRequest, String delimiter, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response listNamespaces(String id, String delimiter, String pageToken, Integer limit, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response listTables(String id, String delimiter, String pageToken, Integer limit, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response namespaceExists(String id, NamespaceExistsRequest orgLanceNamespaceModelNamespaceExistsRequest, String delimiter, SecurityContext securityContext) {
        return null;
    }
}
