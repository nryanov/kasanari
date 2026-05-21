package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestTagService;
import kasanari.authorization.spi.Permission;
import kasanari.instrumentation.spi.paimon.PaimonCatalogOperation;
import kasanari.server.infrastructure.instrumentation.PaimonCatalogRequestExecutor;

import java.util.Map;
import org.apache.paimon.rest.requests.CreateTagRequest;

@ApplicationScoped
public class PaimonTagServiceHandler implements PaimonRestTagService {
    private final PaimonCatalogRouter paimonCatalogRouter;
    private final PaimonCatalogRequestExecutor executor;

    public PaimonTagServiceHandler(PaimonCatalogRouter paimonCatalogRouter, PaimonCatalogRequestExecutor executor) {
        this.paimonCatalogRouter = paimonCatalogRouter;
        this.executor = executor;
    }

    @Override
    public Response createTag(String catalogName, String database, String table, CreateTagRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.CREATE_TAG, Permission.PaimonTagCreate, Map.of("database", database, "table", table),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).createTag(database, table, request);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response deleteTag(String catalogName, String database, String table, String tag, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.DELETE_TAG, Permission.PaimonTagDrop, Map.of("database", database, "table", table, "tag", tag),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).deleteTag(database, table, tag);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response getTag(String catalogName, String database, String table, String tag, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.GET_TAG, Permission.PaimonTagGet, Map.of("database", database, "table", table, "tag", tag),
                () -> {
                    var tagResp = paimonCatalogRouter.getOrThrow(catalogName).getTag(database, table, tag);
                    return Response.ok(tagResp).build();
                }
        );
    }

    @Override
    public Response listTags(String catalogName, String database, String table, Integer maxResults, String pageToken, String tagNamePrefix, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.LIST_TAGS, Permission.PaimonTagList, Map.of("database", database, "table", table),
                () -> {
                    var list = paimonCatalogRouter.getOrThrow(catalogName).listTags(database, table, maxResults, pageToken, tagNamePrefix);
                    return Response.ok(list).build();
                }
        );
    }
}
