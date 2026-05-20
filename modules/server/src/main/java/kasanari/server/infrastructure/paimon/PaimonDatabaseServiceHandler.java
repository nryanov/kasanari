package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.catalog.paimon.api.PaimonRestDatabaseService;
import org.apache.paimon.rest.requests.AlterDatabaseRequest;
import org.apache.paimon.rest.requests.CreateDatabaseRequest;

@ApplicationScoped
public class PaimonDatabaseServiceHandler extends PaimonAuthorizedHandler implements PaimonRestDatabaseService {
    private final PaimonCatalogRouter paimonCatalogRouter;

    public PaimonDatabaseServiceHandler(PaimonCatalogRouter paimonCatalogRouter, AuthorizationService authorizationService) {
        super(authorizationService);
        this.paimonCatalogRouter = paimonCatalogRouter;
    }

    @Override
    public Response alterDatabase(String catalogName, String database, AlterDatabaseRequest request, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonDatabaseAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        var altered = paimonCatalogRouter.getOrThrow(catalogName).alterDatabase(database, request);
        return Response.ok(altered).build();
    }

    @Override
    public Response createDatabase(String catalogName, CreateDatabaseRequest request, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonDatabaseCreate);
        if (denied.isPresent()) {
            return denied.get();
        }
        paimonCatalogRouter.getOrThrow(catalogName).createDatabase(request);
        return Response.ok().build();
    }

    @Override
    public Response dropDatabase(String catalogName, String database, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonDatabaseDrop);
        if (denied.isPresent()) {
            return denied.get();
        }
        paimonCatalogRouter.getOrThrow(catalogName).dropDatabase(database);
        return Response.ok().build();
    }

    @Override
    public Response getDatabase(String catalogName, String database, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonDatabaseGet);
        if (denied.isPresent()) {
            return denied.get();
        }
        var databaseResp = paimonCatalogRouter.getOrThrow(catalogName).getDatabase(database);
        return Response.ok(databaseResp).build();
    }

    @Override
    public Response listDatabases(String catalogName, Integer maxResults, String pageToken, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonDatabaseList);
        if (denied.isPresent()) {
            return denied.get();
        }
        var list = paimonCatalogRouter.getOrThrow(catalogName).listDatabases(maxResults, pageToken);
        return Response.ok(list).build();
    }
}
