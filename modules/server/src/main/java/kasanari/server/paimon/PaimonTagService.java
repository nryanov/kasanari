package kasanari.server.paimon;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestTagService;
import org.apache.paimon.rest.requests.CreateTagRequest;

@ApplicationScoped
public class PaimonTagService implements PaimonRestTagService {
    @Override
    public Response createTag(String prefix, String database, String table, CreateTagRequest orgApachePaimonRestRequestsCreateTagRequest, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response deleteTag(String prefix, String database, String table, String tag, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response getTag(String prefix, String database, String table, String tag, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response listTags(String prefix, String database, String table, Integer maxResults, String pageToken, String tagNamePrefix, SecurityContext securityContext) {
        return null;
    }
}
