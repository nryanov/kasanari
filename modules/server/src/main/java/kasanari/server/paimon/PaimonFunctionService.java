package kasanari.server.paimon;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestFunctionService;
import kasanari.server.http.ApiFallbacks;
import org.apache.paimon.rest.requests.AlterFunctionRequest;
import org.apache.paimon.rest.requests.CreateFunctionRequest;

@ApplicationScoped
public class PaimonFunctionService implements PaimonRestFunctionService {
    @Override
    public Response alterFunction(String prefix, String database, String function, AlterFunctionRequest orgApachePaimonRestRequestsAlterFunctionRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonFunctionService.alterFunction");
    }

    @Override
    public Response createFunction(String prefix, String database, CreateFunctionRequest orgApachePaimonRestRequestsCreateFunctionRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonFunctionService.createFunction");
    }

    @Override
    public Response dropFunction(String prefix, String database, String function, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonFunctionService.dropFunction");
    }

    @Override
    public Response getFunction(String prefix, String database, String function, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonFunctionService.getFunction");
    }

    @Override
    public Response listFunctionDetails(String prefix, String database, Integer maxResults, String pageToken, String functionNamePattern, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonFunctionService.listFunctionDetails");
    }

    @Override
    public Response listFunctions(String prefix, String database, Integer maxResults, String pageToken, String functionNamePattern, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonFunctionService.listFunctions");
    }

    @Override
    public Response listFunctionsGlobally(String prefix, String databaseNamePattern, String functionNamePattern, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonFunctionService.listFunctionsGlobally");
    }
}
