package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.catalog.paimon.api.PaimonRestBranchService;
import org.apache.paimon.rest.requests.CreateBranchRequest;
import org.apache.paimon.rest.requests.ForwardBranchRequest;
import org.apache.paimon.rest.requests.RenameBranchRequest;

@ApplicationScoped
public class PaimonBranchServiceHandler extends PaimonAuthorizedHandler implements PaimonRestBranchService {
    private final PaimonCatalogRouter paimonCatalogRouter;

    public PaimonBranchServiceHandler(PaimonCatalogRouter paimonCatalogRouter, AuthorizationService authorizationService) {
        super(authorizationService);
        this.paimonCatalogRouter = paimonCatalogRouter;
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
}
