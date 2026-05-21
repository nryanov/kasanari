package kasanari.server.infrastructure.lance;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.catalog.lance.api.LanceRestNamespaceService;
import kasanari.repository.management.common.model.CatalogType;
import kasanari.server.infrastructure.http.ApiFallbacks;
import kasanari.server.infrastructure.security.CatalogHandlerAuthorization;

import java.util.Optional;
import org.lance.namespace.model.CreateNamespaceRequest;
import org.lance.namespace.model.DescribeNamespaceRequest;
import org.lance.namespace.model.DropNamespaceRequest;
import org.lance.namespace.model.NamespaceExistsRequest;

@ApplicationScoped
public class LanceNamespaceServiceHandler implements LanceRestNamespaceService {
    private static final CatalogType DOMAIN = CatalogType.LANCE;

    private final AuthorizationService authorizationService;

    public LanceNamespaceServiceHandler(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public Response createNamespace(String id, CreateNamespaceRequest orgLanceNamespaceModelCreateNamespaceRequest, String delimiter, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.LanceNamespaceCreate);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ApiFallbacks.notImplemented("LanceNamespaceService.createNamespace");
    }

    @Override
    public Response describeNamespace(String id, DescribeNamespaceRequest orgLanceNamespaceModelDescribeNamespaceRequest, String delimiter, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.LanceNamespaceGet);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ApiFallbacks.notImplemented("LanceNamespaceService.describeNamespace");
    }

    @Override
    public Response dropNamespace(String id, DropNamespaceRequest orgLanceNamespaceModelDropNamespaceRequest, String delimiter, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.LanceNamespaceDrop);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ApiFallbacks.notImplemented("LanceNamespaceService.dropNamespace");
    }

    @Override
    public Response listNamespaces(String id, String delimiter, String pageToken, Integer limit, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.LanceNamespaceList);
        if (denied.isPresent()) {
            return denied.get();
        }
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
        var denied = deny(securityContext, Permission.LanceTableList);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ApiFallbacks.notImplemented("LanceNamespaceService.listTables");
    }

    @Override
    public Response namespaceExists(String id, NamespaceExistsRequest orgLanceNamespaceModelNamespaceExistsRequest, String delimiter, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.LanceNamespaceExists);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ApiFallbacks.notImplemented("LanceNamespaceService.namespaceExists");
    }

    private Optional<Response> deny(SecurityContext securityContext, Permission permission) {
        return CatalogHandlerAuthorization.denyUnless(authorizationService, securityContext, DOMAIN, permission);
    }
}
