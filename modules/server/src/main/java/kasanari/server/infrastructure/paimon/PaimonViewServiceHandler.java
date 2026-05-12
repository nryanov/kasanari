package kasanari.server.infrastructure.paimon;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestViewService;
import kasanari.server.infrastructure.http.ApiFallbacks;
import org.apache.paimon.rest.requests.AlterViewRequest;
import org.apache.paimon.rest.requests.CreateViewRequest;
import org.apache.paimon.rest.requests.RenameTableRequest;

@ApplicationScoped
public class PaimonViewServiceHandler implements PaimonRestViewService {
    @Override
    public Response alterView(String prefix, String database, String view, AlterViewRequest orgApachePaimonRestRequestsAlterViewRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonViewService.alterView");
    }

    @Override
    public Response createView(String prefix, String database, CreateViewRequest orgApachePaimonRestRequestsCreateViewRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonViewService.createView");
    }

    @Override
    public Response dropView(String prefix, String database, String view, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonViewService.dropView");
    }

    @Override
    public Response getView(String prefix, String database, String view, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonViewService.getView");
    }

    @Override
    public Response listViewDetails(String prefix, String database, Integer maxResults, String pageToken, String viewNamePattern, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonViewService.listViewDetails");
    }

    @Override
    public Response listViews(String prefix, String database, Integer maxResults, String pageToken, String viewNamePattern, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonViewService.listViews");
    }

    @Override
    public Response listViewsGlobally(String prefix, String databaseNamePattern, String viewNamePattern, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonViewService.listViewsGlobally");
    }

    @Override
    public Response renameView(String prefix, RenameTableRequest orgApachePaimonRestRequestsRenameTableRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonViewService.renameView");
    }
}
