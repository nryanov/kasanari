package kasanari.server.paimon;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestFunctionService;
import org.apache.paimon.rest.requests.AlterFunctionRequest;
import org.apache.paimon.rest.requests.CreateFunctionRequest;

@ApplicationScoped
public class PaimonFunctionService implements PaimonRestFunctionService {
    @Override
    public Response alterFunction(String prefix, String database, String function, AlterFunctionRequest orgApachePaimonRestRequestsAlterFunctionRequest, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response createFunction(String prefix, String database, CreateFunctionRequest orgApachePaimonRestRequestsCreateFunctionRequest, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response dropFunction(String prefix, String database, String function, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response getFunction(String prefix, String database, String function, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response listFunctionDetails(String prefix, String database, Integer maxResults, String pageToken, String functionNamePattern, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response listFunctions(String prefix, String database, Integer maxResults, String pageToken, String functionNamePattern, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response listFunctionsGlobally(String prefix, String databaseNamePattern, String functionNamePattern, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return null;
    }
}
