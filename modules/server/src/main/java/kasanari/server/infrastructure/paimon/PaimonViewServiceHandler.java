package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.catalog.paimon.api.PaimonRestViewService;
import kasanari.core.model.CatalogType;
import kasanari.server.infrastructure.security.CatalogHandlerAuthorization;

import java.util.Optional;
import org.apache.paimon.rest.requests.AlterViewRequest;
import org.apache.paimon.rest.requests.CreateViewRequest;
import org.apache.paimon.rest.requests.RenameTableRequest;

@ApplicationScoped
public class PaimonViewServiceHandler implements PaimonRestViewService {
    private static final CatalogType DOMAIN = CatalogType.PAIMON;

    private final PaimonCatalogRouter paimonCatalogRouter;
    private final AuthorizationService authorizationService;

    public PaimonViewServiceHandler(PaimonCatalogRouter paimonCatalogRouter, AuthorizationService authorizationService) {
        this.paimonCatalogRouter = paimonCatalogRouter;
        this.authorizationService = authorizationService;
    }

    @Override
    public Response alterView(String catalogName, String database, String view, AlterViewRequest request, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonViewAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        paimonCatalogRouter.getOrThrow(catalogName).alterView(database, view, request);
        return Response.ok().build();
    }

    @Override
    public Response createView(String catalogName, String database, CreateViewRequest request, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonViewCreate);
        if (denied.isPresent()) {
            return denied.get();
        }
        paimonCatalogRouter.getOrThrow(catalogName).createView(database, request);
        return Response.ok().build();
    }

    @Override
    public Response dropView(String catalogName, String database, String view, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonViewDrop);
        if (denied.isPresent()) {
            return denied.get();
        }
        paimonCatalogRouter.getOrThrow(catalogName).dropView(database, view);
        return Response.ok().build();
    }

    @Override
    public Response getView(String catalogName, String database, String view, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonViewGet);
        if (denied.isPresent()) {
            return denied.get();
        }
        var viewResp = paimonCatalogRouter.getOrThrow(catalogName).getView(database, view);
        return Response.ok(viewResp).build();
    }

    @Override
    public Response listViewDetails(String catalogName, String database, Integer maxResults, String pageToken, String viewNamePattern, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonViewList);
        if (denied.isPresent()) {
            return denied.get();
        }
        var list = paimonCatalogRouter.getOrThrow(catalogName).listViewDetails(database, maxResults, pageToken, viewNamePattern);
        return Response.ok(list).build();
    }

    @Override
    public Response listViews(String catalogName, String database, Integer maxResults, String pageToken, String viewNamePattern, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonViewList);
        if (denied.isPresent()) {
            return denied.get();
        }
        var list = paimonCatalogRouter.getOrThrow(catalogName).listViews(database, maxResults, pageToken, viewNamePattern);
        return Response.ok(list).build();
    }

    @Override
    public Response listViewsGlobally(String catalogName, String databaseNamePattern, String viewNamePattern, Integer maxResults, String pageToken, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonViewList);
        if (denied.isPresent()) {
            return denied.get();
        }
        var list = paimonCatalogRouter.getOrThrow(catalogName).listViewsGlobally(databaseNamePattern, viewNamePattern, maxResults, pageToken);
        return Response.ok(list).build();
    }

    @Override
    public Response renameView(String catalogName, RenameTableRequest request, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonViewAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        paimonCatalogRouter.getOrThrow(catalogName).renameView(request);
        return Response.ok().build();
    }

    private Optional<Response> deny(SecurityContext securityContext, Permission permission) {
        return CatalogHandlerAuthorization.denyUnless(authorizationService, securityContext, DOMAIN, permission);
    }
}
