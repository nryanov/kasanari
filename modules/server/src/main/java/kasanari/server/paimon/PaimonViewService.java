package kasanari.server.paimon;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestViewService;
import org.apache.paimon.rest.requests.AlterViewRequest;
import org.apache.paimon.rest.requests.CreateViewRequest;
import org.apache.paimon.rest.requests.RenameTableRequest;

@ApplicationScoped
public class PaimonViewService implements PaimonRestViewService {
    @Override
    public Response alterView(String prefix, String database, String view, AlterViewRequest orgApachePaimonRestRequestsAlterViewRequest, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response createView(String prefix, String database, CreateViewRequest orgApachePaimonRestRequestsCreateViewRequest, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response dropView(String prefix, String database, String view, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response getView(String prefix, String database, String view, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response listViewDetails(String prefix, String database, Integer maxResults, String pageToken, String viewNamePattern, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response listViews(String prefix, String database, Integer maxResults, String pageToken, String viewNamePattern, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response listViewsGlobally(String prefix, String databaseNamePattern, String viewNamePattern, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response renameView(String prefix, RenameTableRequest orgApachePaimonRestRequestsRenameTableRequest, SecurityContext securityContext) {
        return null;
    }
}
