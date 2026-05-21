package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestFunctionService;
import kasanari.authorization.spi.Permission;
import kasanari.instrumentation.spi.paimon.PaimonCatalogOperation;
import kasanari.server.infrastructure.instrumentation.PaimonCatalogRequestExecutor;

import java.util.Map;
import org.apache.paimon.rest.requests.AlterFunctionRequest;
import org.apache.paimon.rest.requests.CreateFunctionRequest;

@ApplicationScoped
public class PaimonFunctionServiceHandler implements PaimonRestFunctionService {
    private final PaimonCatalogRouter paimonCatalogRouter;
    private final PaimonCatalogRequestExecutor executor;

    public PaimonFunctionServiceHandler(PaimonCatalogRouter paimonCatalogRouter, PaimonCatalogRequestExecutor executor) {
        this.paimonCatalogRouter = paimonCatalogRouter;
        this.executor = executor;
    }

    @Override
    public Response alterFunction(String catalogName, String database, String function, AlterFunctionRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.ALTER_FUNCTION, Permission.PaimonFunctionAlter, Map.of("database", database, "function", function),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).alterFunction(database, function, request);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response createFunction(String catalogName, String database, CreateFunctionRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.CREATE_FUNCTION, Permission.PaimonFunctionCreate, Map.of("database", database),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).createFunction(database, request);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response dropFunction(String catalogName, String database, String function, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.DROP_FUNCTION, Permission.PaimonFunctionDrop, Map.of("database", database, "function", function),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).dropFunction(database, function);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response getFunction(String catalogName, String database, String function, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.GET_FUNCTION, Permission.PaimonFunctionGet, Map.of("database", database, "function", function),
                () -> {
                    var functionResp = paimonCatalogRouter.getOrThrow(catalogName).getFunction(database, function);
                    return Response.ok(functionResp).build();
                }
        );
    }

    @Override
    public Response listFunctionDetails(String catalogName, String database, Integer maxResults, String pageToken, String functionNamePattern, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.LIST_FUNCTION_DETAILS, Permission.PaimonFunctionList, Map.of("database", database),
                () -> {
                    var list = paimonCatalogRouter.getOrThrow(catalogName).listFunctionDetails(database, maxResults, pageToken, functionNamePattern);
                    return Response.ok(list).build();
                }
        );
    }

    @Override
    public Response listFunctions(String catalogName, String database, Integer maxResults, String pageToken, String functionNamePattern, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.LIST_FUNCTIONS, Permission.PaimonFunctionList, Map.of("database", database),
                () -> {
                    var list = paimonCatalogRouter.getOrThrow(catalogName).listFunctions(database, maxResults, pageToken, functionNamePattern);
                    return Response.ok(list).build();
                }
        );
    }

    @Override
    public Response listFunctionsGlobally(String catalogName, String databaseNamePattern, String functionNamePattern, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.LIST_FUNCTIONS_GLOBALLY, Permission.PaimonFunctionList, Map.of(), () -> {
            var list =
                    paimonCatalogRouter.getOrThrow(catalogName).listFunctionsGlobally(databaseNamePattern, functionNamePattern, maxResults, pageToken);
            return Response.ok(list).build();
        });
    }
}
