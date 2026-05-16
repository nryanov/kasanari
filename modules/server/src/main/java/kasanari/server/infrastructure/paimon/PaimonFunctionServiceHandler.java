package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestFunctionService;
import org.apache.paimon.rest.requests.AlterFunctionRequest;
import org.apache.paimon.rest.requests.CreateFunctionRequest;

@ApplicationScoped
public class PaimonFunctionServiceHandler implements PaimonRestFunctionService {
    private final PaimonCatalogRouter paimonCatalogRouter;

    public PaimonFunctionServiceHandler(PaimonCatalogRouter paimonCatalogRouter) {
        this.paimonCatalogRouter = paimonCatalogRouter;
    }

    @Override
    public Response alterFunction(String catalogName, String database, String function, AlterFunctionRequest request, SecurityContext securityContext) {
        paimonCatalogRouter.getOrThrow(catalogName).alterFunction(database, function, request);
        return Response.ok().build();
    }

    @Override
    public Response createFunction(String catalogName, String database, CreateFunctionRequest request, SecurityContext securityContext) {
        paimonCatalogRouter.getOrThrow(catalogName).createFunction(database, request);
        return Response.ok().build();
    }

    @Override
    public Response dropFunction(String catalogName, String database, String function, SecurityContext securityContext) {
        paimonCatalogRouter.getOrThrow(catalogName).dropFunction(database, function);
        return Response.ok().build();
    }

    @Override
    public Response getFunction(String catalogName, String database, String function, SecurityContext securityContext) {
        var functionResp = paimonCatalogRouter.getOrThrow(catalogName).getFunction(database, function);
        return Response.ok(functionResp).build();
    }

    @Override
    public Response listFunctionDetails(String catalogName, String database, Integer maxResults, String pageToken, String functionNamePattern, SecurityContext securityContext) {
        var list = paimonCatalogRouter.getOrThrow(catalogName).listFunctionDetails(database, maxResults, pageToken, functionNamePattern);
        return Response.ok(list).build();
    }

    @Override
    public Response listFunctions(String catalogName, String database, Integer maxResults, String pageToken, String functionNamePattern, SecurityContext securityContext) {
        var list = paimonCatalogRouter.getOrThrow(catalogName).listFunctions(database, maxResults, pageToken, functionNamePattern);
        return Response.ok(list).build();
    }

    @Override
    public Response listFunctionsGlobally(String catalogName, String databaseNamePattern, String functionNamePattern, Integer maxResults, String pageToken, SecurityContext securityContext) {
        var list =
                paimonCatalogRouter.getOrThrow(catalogName).listFunctionsGlobally(databaseNamePattern, functionNamePattern, maxResults, pageToken);
        return Response.ok(list).build();
    }
}
