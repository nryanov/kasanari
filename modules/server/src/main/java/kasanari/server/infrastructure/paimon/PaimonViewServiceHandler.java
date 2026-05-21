package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestViewService;
import kasanari.authorization.spi.Permission;
import kasanari.instrumentation.spi.paimon.PaimonCatalogOperation;
import kasanari.server.infrastructure.instrumentation.PaimonCatalogRequestExecutor;

import java.util.Map;
import org.apache.paimon.rest.requests.AlterViewRequest;
import org.apache.paimon.rest.requests.CreateViewRequest;
import org.apache.paimon.rest.requests.RenameTableRequest;

@ApplicationScoped
public class PaimonViewServiceHandler implements PaimonRestViewService {
    private final PaimonCatalogRouter paimonCatalogRouter;
    private final PaimonCatalogRequestExecutor executor;

    public PaimonViewServiceHandler(PaimonCatalogRouter paimonCatalogRouter, PaimonCatalogRequestExecutor executor) {
        this.paimonCatalogRouter = paimonCatalogRouter;
        this.executor = executor;
    }

    @Override
    public Response alterView(String catalogName, String database, String view, AlterViewRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.ALTER_VIEW, Permission.PaimonViewAlter, Map.of("database", database, "view", view),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).alterView(database, view, request);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response createView(String catalogName, String database, CreateViewRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.CREATE_VIEW, Permission.PaimonViewCreate, Map.of("database", database),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).createView(database, request);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response dropView(String catalogName, String database, String view, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.DROP_VIEW, Permission.PaimonViewDrop, Map.of("database", database, "view", view),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).dropView(database, view);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response getView(String catalogName, String database, String view, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.GET_VIEW, Permission.PaimonViewGet, Map.of("database", database, "view", view),
                () -> {
                    var viewResp = paimonCatalogRouter.getOrThrow(catalogName).getView(database, view);
                    return Response.ok(viewResp).build();
                }
        );
    }

    @Override
    public Response listViewDetails(String catalogName, String database, Integer maxResults, String pageToken, String viewNamePattern, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.LIST_VIEW_DETAILS, Permission.PaimonViewList, Map.of("database", database),
                () -> {
                    var list = paimonCatalogRouter.getOrThrow(catalogName).listViewDetails(database, maxResults, pageToken, viewNamePattern);
                    return Response.ok(list).build();
                }
        );
    }

    @Override
    public Response listViews(String catalogName, String database, Integer maxResults, String pageToken, String viewNamePattern, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.LIST_VIEWS, Permission.PaimonViewList, Map.of("database", database),
                () -> {
                    var list = paimonCatalogRouter.getOrThrow(catalogName).listViews(database, maxResults, pageToken, viewNamePattern);
                    return Response.ok(list).build();
                }
        );
    }

    @Override
    public Response listViewsGlobally(String catalogName, String databaseNamePattern, String viewNamePattern, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.LIST_VIEWS_GLOBALLY, Permission.PaimonViewList, Map.of(), () -> {
            var list = paimonCatalogRouter.getOrThrow(catalogName).listViewsGlobally(databaseNamePattern, viewNamePattern, maxResults, pageToken);
            return Response.ok(list).build();
        });
    }

    @Override
    public Response renameView(String catalogName, RenameTableRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.RENAME_VIEW, Permission.PaimonViewAlter, Map.of(), () -> {
            paimonCatalogRouter.getOrThrow(catalogName).renameView(request);
            return Response.ok().build();
        });
    }
}
