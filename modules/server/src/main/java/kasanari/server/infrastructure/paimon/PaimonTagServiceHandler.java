package kasanari.server.infrastructure.paimon;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestTagService;
import kasanari.server.infrastructure.http.ApiFallbacks;
import org.apache.paimon.rest.requests.CreateTagRequest;

@ApplicationScoped
public class PaimonTagServiceHandler implements PaimonRestTagService {
    @Override
    public Response createTag(String prefix, String database, String table, CreateTagRequest orgApachePaimonRestRequestsCreateTagRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTagService.createTag");
    }

    @Override
    public Response deleteTag(String prefix, String database, String table, String tag, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTagService.deleteTag");
    }

    @Override
    public Response getTag(String prefix, String database, String table, String tag, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTagService.getTag");
    }

    @Override
    public Response listTags(String prefix, String database, String table, Integer maxResults, String pageToken, String tagNamePrefix, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTagService.listTags");
    }
}
