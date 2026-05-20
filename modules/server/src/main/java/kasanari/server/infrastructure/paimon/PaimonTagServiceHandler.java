package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.catalog.paimon.api.PaimonRestTagService;
import org.apache.paimon.rest.requests.CreateTagRequest;

@ApplicationScoped
public class PaimonTagServiceHandler extends PaimonAuthorizedHandler implements PaimonRestTagService {
    private final PaimonCatalogRouter paimonCatalogRouter;

    public PaimonTagServiceHandler(PaimonCatalogRouter paimonCatalogRouter, AuthorizationService authorizationService) {
        super(authorizationService);
        this.paimonCatalogRouter = paimonCatalogRouter;
    }

    @Override
    public Response createTag(String catalogName, String database, String table, CreateTagRequest request, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonTagCreate);
        if (denied.isPresent()) {
            return denied.get();
        }
        paimonCatalogRouter.getOrThrow(catalogName).createTag(database, table, request);
        return Response.ok().build();
    }

    @Override
    public Response deleteTag(String catalogName, String database, String table, String tag, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonTagDrop);
        if (denied.isPresent()) {
            return denied.get();
        }
        paimonCatalogRouter.getOrThrow(catalogName).deleteTag(database, table, tag);
        return Response.ok().build();
    }

    @Override
    public Response getTag(String catalogName, String database, String table, String tag, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonTagGet);
        if (denied.isPresent()) {
            return denied.get();
        }
        var tagResp = paimonCatalogRouter.getOrThrow(catalogName).getTag(database, table, tag);
        return Response.ok(tagResp).build();
    }

    @Override
    public Response listTags(String catalogName, String database, String table, Integer maxResults, String pageToken, String tagNamePrefix, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonTagList);
        if (denied.isPresent()) {
            return denied.get();
        }
        var list = paimonCatalogRouter.getOrThrow(catalogName).listTags(database, table, maxResults, pageToken, tagNamePrefix);
        return Response.ok(list).build();
    }
}
