package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.catalog.paimon.api.PaimonRestFunctionService;
import kasanari.repository.management.common.model.CatalogType;
import kasanari.server.infrastructure.security.CatalogHandlerAuthorization;

import java.util.Optional;
import org.apache.paimon.rest.requests.AlterFunctionRequest;
import org.apache.paimon.rest.requests.CreateFunctionRequest;

@ApplicationScoped
public class PaimonFunctionServiceHandler implements PaimonRestFunctionService {
    private static final CatalogType DOMAIN = CatalogType.PAIMON;

    private final PaimonCatalogRouter paimonCatalogRouter;
    private final AuthorizationService authorizationService;

    public PaimonFunctionServiceHandler(PaimonCatalogRouter paimonCatalogRouter, AuthorizationService authorizationService) {
        this.paimonCatalogRouter = paimonCatalogRouter;
        this.authorizationService = authorizationService;
    }

    @Override
    public Response alterFunction(String catalogName, String database, String function, AlterFunctionRequest request, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonFunctionAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        paimonCatalogRouter.getOrThrow(catalogName).alterFunction(database, function, request);
        return Response.ok().build();
    }

    @Override
    public Response createFunction(String catalogName, String database, CreateFunctionRequest request, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonFunctionCreate);
        if (denied.isPresent()) {
            return denied.get();
        }
        paimonCatalogRouter.getOrThrow(catalogName).createFunction(database, request);
        return Response.ok().build();
    }

    @Override
    public Response dropFunction(String catalogName, String database, String function, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonFunctionDrop);
        if (denied.isPresent()) {
            return denied.get();
        }
        paimonCatalogRouter.getOrThrow(catalogName).dropFunction(database, function);
        return Response.ok().build();
    }

    @Override
    public Response getFunction(String catalogName, String database, String function, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonFunctionGet);
        if (denied.isPresent()) {
            return denied.get();
        }
        var functionResp = paimonCatalogRouter.getOrThrow(catalogName).getFunction(database, function);
        return Response.ok(functionResp).build();
    }

    @Override
    public Response listFunctionDetails(String catalogName, String database, Integer maxResults, String pageToken, String functionNamePattern, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonFunctionList);
        if (denied.isPresent()) {
            return denied.get();
        }
        var list = paimonCatalogRouter.getOrThrow(catalogName).listFunctionDetails(database, maxResults, pageToken, functionNamePattern);
        return Response.ok(list).build();
    }

    @Override
    public Response listFunctions(String catalogName, String database, Integer maxResults, String pageToken, String functionNamePattern, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonFunctionList);
        if (denied.isPresent()) {
            return denied.get();
        }
        var list = paimonCatalogRouter.getOrThrow(catalogName).listFunctions(database, maxResults, pageToken, functionNamePattern);
        return Response.ok(list).build();
    }

    @Override
    public Response listFunctionsGlobally(String catalogName, String databaseNamePattern, String functionNamePattern, Integer maxResults, String pageToken, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonFunctionList);
        if (denied.isPresent()) {
            return denied.get();
        }
        var list =
                paimonCatalogRouter.getOrThrow(catalogName).listFunctionsGlobally(databaseNamePattern, functionNamePattern, maxResults, pageToken);
        return Response.ok(list).build();
    }

    private Optional<Response> deny(SecurityContext securityContext, Permission permission) {
        return CatalogHandlerAuthorization.denyUnless(authorizationService, securityContext, DOMAIN, permission);
    }
}
