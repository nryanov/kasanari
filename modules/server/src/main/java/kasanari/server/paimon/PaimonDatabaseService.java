package kasanari.server.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestDatabaseService;
import kasanari.server.http.ApiFallbacks;
import org.apache.paimon.rest.requests.AlterDatabaseRequest;
import org.apache.paimon.rest.requests.CreateDatabaseRequest;

@ApplicationScoped
public class PaimonDatabaseService implements PaimonRestDatabaseService {
    @Override
    public Response alterDatabase(String prefix, String database, AlterDatabaseRequest orgApachePaimonRestRequestsAlterDatabaseRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonDatabaseService.alterDatabase");
    }

    @Override
    public Response createDatabase(String prefix, CreateDatabaseRequest orgApachePaimonRestRequestsCreateDatabaseRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonDatabaseService.createDatabase");
    }

    @Override
    public Response dropDatabase(String prefix, String database, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonDatabaseService.dropDatabase");
    }

    @Override
    public Response getDatabase(String prefix, String database, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonDatabaseService.getDatabase");
    }

    @Override
    public Response listDatabases(String prefix, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonDatabaseService.listDatabases");
    }
}
