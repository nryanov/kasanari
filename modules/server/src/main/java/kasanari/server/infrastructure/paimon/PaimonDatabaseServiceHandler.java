package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestDatabaseService;
import kasanari.authorization.spi.Permission;
import kasanari.instrumentation.spi.paimon.PaimonCatalogOperation;
import kasanari.server.infrastructure.instrumentation.PaimonCatalogRequestExecutor;

import java.util.Map;
import org.apache.paimon.rest.requests.AlterDatabaseRequest;
import org.apache.paimon.rest.requests.CreateDatabaseRequest;

@ApplicationScoped
public class PaimonDatabaseServiceHandler implements PaimonRestDatabaseService {
    private final PaimonCatalogRouter paimonCatalogRouter;
    private final PaimonCatalogRequestExecutor executor;

    public PaimonDatabaseServiceHandler(PaimonCatalogRouter paimonCatalogRouter, PaimonCatalogRequestExecutor executor) {
        this.paimonCatalogRouter = paimonCatalogRouter;
        this.executor = executor;
    }

    @Override
    public Response alterDatabase(String catalogName, String database, AlterDatabaseRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.ALTER_DATABASE, Permission.PaimonDatabaseAlter, Map.of("database", database),
                () -> {
                    var altered = paimonCatalogRouter.getOrThrow(catalogName).alterDatabase(database, request);
                    return Response.ok(altered).build();
                }
        );
    }

    @Override
    public Response createDatabase(String catalogName, CreateDatabaseRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.CREATE_DATABASE, Permission.PaimonDatabaseCreate, Map.of(), () -> {
            paimonCatalogRouter.getOrThrow(catalogName).createDatabase(request);
            return Response.ok().build();
        });
    }

    @Override
    public Response dropDatabase(String catalogName, String database, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.DROP_DATABASE, Permission.PaimonDatabaseDrop, Map.of("database", database),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).dropDatabase(database);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response getDatabase(String catalogName, String database, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.GET_DATABASE, Permission.PaimonDatabaseGet, Map.of("database", database),
                () -> {
                    var databaseResp = paimonCatalogRouter.getOrThrow(catalogName).getDatabase(database);
                    return Response.ok(databaseResp).build();
                }
        );
    }

    @Override
    public Response listDatabases(String catalogName, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.LIST_DATABASES, Permission.PaimonDatabaseList, Map.of(), () -> {
            var list = paimonCatalogRouter.getOrThrow(catalogName).listDatabases(maxResults, pageToken);
            return Response.ok(list).build();
        });
    }
}
