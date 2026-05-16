package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestTableService;
import org.apache.paimon.rest.requests.AlterTableRequest;
import org.apache.paimon.rest.requests.AuthTableQueryRequest;
import org.apache.paimon.rest.requests.CommitTableRequest;
import org.apache.paimon.rest.requests.CreateTableRequest;
import org.apache.paimon.rest.requests.RegisterTableRequest;
import org.apache.paimon.rest.requests.RenameTableRequest;
import org.apache.paimon.rest.requests.ResetConsumerRequest;
import org.apache.paimon.rest.requests.RollbackSchemaRequest;
import org.apache.paimon.rest.requests.RollbackTableRequest;

@ApplicationScoped
public class PaimonTableServiceHandler implements PaimonRestTableService {
    private final PaimonCatalogRouter paimonCatalogRouter;

    public PaimonTableServiceHandler(PaimonCatalogRouter paimonCatalogRouter) {
        this.paimonCatalogRouter = paimonCatalogRouter;
    }

    @Override
    public Response alterTable(String catalogName, String database, String table, AlterTableRequest request, SecurityContext securityContext) {
        paimonCatalogRouter.getOrThrow(catalogName).alterTable(database, table, request);
        return Response.ok().build();
    }

    @Override
    public Response authTableQuery(String catalogName, String database, String table, AuthTableQueryRequest request, SecurityContext securityContext) {
        var result = paimonCatalogRouter.getOrThrow(catalogName).authTableQuery(database, table, request);
        return Response.ok(result).build();
    }

    @Override
    public Response commitTable(String catalogName, String database, String table, CommitTableRequest request, SecurityContext securityContext) {
        var result = paimonCatalogRouter.getOrThrow(catalogName).commitTable(database, table, request);
        return Response.ok(result).build();
    }

    @Override
    public Response createTable(String catalogName, String database, CreateTableRequest request, SecurityContext securityContext) {
        paimonCatalogRouter.getOrThrow(catalogName).createTable(database, request);
        return Response.ok().build();
    }

    @Override
    public Response dropTable(String catalogName, String database, String table, SecurityContext securityContext) {
        paimonCatalogRouter.getOrThrow(catalogName).dropTable(database, table);
        return Response.ok().build();
    }

    @Override
    public Response getTable(String catalogName, String database, String table, SecurityContext securityContext) {
        var tableResp = paimonCatalogRouter.getOrThrow(catalogName).getTable(database, table);
        return Response.ok(tableResp).build();
    }

    @Override
    public Response getTableById(String catalogName, String tableId, SecurityContext securityContext) {
        var tableResp = paimonCatalogRouter.getOrThrow(catalogName).getTableById(tableId);
        return Response.ok(tableResp).build();
    }

    @Override
    public Response getTableSnapshot(String catalogName, String database, String table, SecurityContext securityContext) {
        var snapshot = paimonCatalogRouter.getOrThrow(catalogName).getTableSnapshot(database, table);
        return Response.ok(snapshot).build();
    }

    @Override
    public Response getTableToken(String catalogName, String database, String table, SecurityContext securityContext) {
        var token = paimonCatalogRouter.getOrThrow(catalogName).getTableToken(database, table);
        return Response.ok(token).build();
    }

    @Override
    public Response getVersionSnapshot(String catalogName, String database, String table, String version, SecurityContext securityContext) {
        var snapshot = paimonCatalogRouter.getOrThrow(catalogName).getVersionSnapshot(database, table, version);
        return Response.ok(snapshot).build();
    }

    @Override
    public Response listConsumers(String catalogName, String database, String table, Integer maxResults, String pageToken, SecurityContext securityContext) {
        var list = paimonCatalogRouter.getOrThrow(catalogName).listConsumers(database, table, maxResults, pageToken);
        return Response.ok(list).build();
    }

    @Override
    public Response listSnapshots(String catalogName, String database, String table, Integer maxResults, String pageToken, SecurityContext securityContext) {
        var list = paimonCatalogRouter.getOrThrow(catalogName).listSnapshots(database, table, maxResults, pageToken);
        return Response.ok(list).build();
    }

    @Override
    public Response listTableDetails(String catalogName, String database, Integer maxResults, String pageToken, String tableNamePattern, String tableType, SecurityContext securityContext) {
        var list = paimonCatalogRouter.getOrThrow(catalogName).listTableDetails(database, maxResults, pageToken, tableNamePattern, tableType);
        return Response.ok(list).build();
    }

    @Override
    public Response listTables(String catalogName, String database, Integer maxResults, String pageToken, String tableNamePattern, SecurityContext securityContext) {
        var list = paimonCatalogRouter.getOrThrow(catalogName).listTables(database, maxResults, pageToken, tableNamePattern);
        return Response.ok(list).build();
    }

    @Override
    public Response listTablesGlobally(String catalogName, String databaseNamePattern, String tableNamePattern, Integer maxResults, String pageToken, SecurityContext securityContext) {
        var list = paimonCatalogRouter.getOrThrow(catalogName).listTablesGlobally(databaseNamePattern, tableNamePattern, maxResults, pageToken);
        return Response.ok(list).build();
    }

    @Override
    public Response registerTable(String catalogName, String database, RegisterTableRequest request, SecurityContext securityContext) {
        paimonCatalogRouter.getOrThrow(catalogName).registerTable(database, request);
        return Response.ok().build();
    }

    @Override
    public Response renameTable(String catalogName, RenameTableRequest request, SecurityContext securityContext) {
        paimonCatalogRouter.getOrThrow(catalogName).renameTable(request);
        return Response.ok().build();
    }

    @Override
    public Response resetConsumer(String catalogName, String database, String table, ResetConsumerRequest request, SecurityContext securityContext) {
        paimonCatalogRouter.getOrThrow(catalogName).resetConsumer(database, table, request);
        return Response.ok().build();
    }

    @Override
    public Response rollbackSchema(String catalogName, String database, String table, RollbackSchemaRequest request, SecurityContext securityContext) {
        paimonCatalogRouter.getOrThrow(catalogName).rollbackSchema(database, table, request);
        return Response.ok().build();
    }

    @Override
    public Response rollbackTable(String catalogName, String database, String table, RollbackTableRequest request, SecurityContext securityContext) {
        paimonCatalogRouter.getOrThrow(catalogName).rollbackTable(database, table, request);
        return Response.ok().build();
    }
}
