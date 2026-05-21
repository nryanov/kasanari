package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.catalog.paimon.api.PaimonRestBranchService;
import kasanari.core.model.CatalogType;
import kasanari.server.infrastructure.security.CatalogHandlerAuthorization;

import java.util.Optional;
import org.apache.paimon.rest.requests.CreateBranchRequest;
import org.apache.paimon.rest.requests.ForwardBranchRequest;
import org.apache.paimon.rest.requests.RenameBranchRequest;

@ApplicationScoped
public class PaimonBranchServiceHandler implements PaimonRestBranchService {
    private static final CatalogType DOMAIN = CatalogType.PAIMON;

    private final PaimonCatalogRouter paimonCatalogRouter;
    private final AuthorizationService authorizationService;

    public PaimonBranchServiceHandler(PaimonCatalogRouter paimonCatalogRouter, AuthorizationService authorizationService) {
        this.paimonCatalogRouter = paimonCatalogRouter;
        this.authorizationService = authorizationService;
    }

    @Override
    public Response createBranch(String catalogName, String database, String table, CreateBranchRequest request, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonBranchCreate);
        if (denied.isPresent()) {
            return denied.get();
        }
        paimonCatalogRouter.getOrThrow(catalogName).createBranch(database, table, request);
        return Response.ok().build();
    }

    @Override
    public Response dropBranch(String catalogName, String database, String table, String branch, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonBranchDrop);
        if (denied.isPresent()) {
            return denied.get();
        }
        paimonCatalogRouter.getOrThrow(catalogName).dropBranch(database, table, branch);
        return Response.ok().build();
    }

    @Override
    public Response forwardBranch(String catalogName, String database, String table, String branch, ForwardBranchRequest request, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonBranchAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        paimonCatalogRouter.getOrThrow(catalogName).forwardBranch(database, table, branch, request);
        return Response.ok().build();
    }

    @Override
    public Response listBranches(String catalogName, String database, String table, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonBranchList);
        if (denied.isPresent()) {
            return denied.get();
        }
        var list = paimonCatalogRouter.getOrThrow(catalogName).listBranches(database, table);
        return Response.ok(list).build();
    }

    @Override
    public Response renameBranch(String catalogName, String database, String table, String branch, RenameBranchRequest request, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonBranchAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        paimonCatalogRouter.getOrThrow(catalogName).renameBranch(database, table, branch, request);
        return Response.ok().build();
    }

    private Optional<Response> deny(SecurityContext securityContext, Permission permission) {
        return CatalogHandlerAuthorization.denyUnless(authorizationService, securityContext, DOMAIN, permission);
    }
}
