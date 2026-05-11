package kasanari.server.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestDatabaseService;
import org.apache.paimon.rest.requests.AlterDatabaseRequest;
import org.apache.paimon.rest.requests.CreateDatabaseRequest;

@ApplicationScoped
public class PaimonDatabaseService implements PaimonRestDatabaseService {
    @Override
    public Response alterDatabase(String prefix, String database, AlterDatabaseRequest orgApachePaimonRestRequestsAlterDatabaseRequest, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response createDatabase(String prefix, CreateDatabaseRequest orgApachePaimonRestRequestsCreateDatabaseRequest, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response dropDatabase(String prefix, String database, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response getDatabase(String prefix, String database, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response listDatabases(String prefix, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return null;
    }
}
