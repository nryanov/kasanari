package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestTagService;
import org.apache.paimon.rest.requests.CreateTagRequest;

@ApplicationScoped
public class PaimonTagServiceHandler implements PaimonRestTagService {
    private final PaimonCatalogRouter paimonCatalogRouter;

    public PaimonTagServiceHandler(PaimonCatalogRouter paimonCatalogRouter) {
        this.paimonCatalogRouter = paimonCatalogRouter;
    }

    @Override
    public Response createTag(String catalogName, String database, String table, CreateTagRequest request, SecurityContext securityContext) {
        paimonCatalogRouter.getOrThrow(catalogName).createTag(database, table, request);
        return Response.ok().build();
    }

    @Override
    public Response deleteTag(String catalogName, String database, String table, String tag, SecurityContext securityContext) {
        paimonCatalogRouter.getOrThrow(catalogName).deleteTag(database, table, tag);
        return Response.ok().build();
    }

    @Override
    public Response getTag(String catalogName, String database, String table, String tag, SecurityContext securityContext) {
        var tagResp = paimonCatalogRouter.getOrThrow(catalogName).getTag(database, table, tag);
        return Response.ok(tagResp).build();
    }

    @Override
    public Response listTags(String catalogName, String database, String table, Integer maxResults, String pageToken, String tagNamePrefix, SecurityContext securityContext) {
        var list = paimonCatalogRouter.getOrThrow(catalogName).listTags(database, table, maxResults, pageToken, tagNamePrefix);
        return Response.ok(list).build();
    }
}
