package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestTableService;
import kasanari.authorization.spi.Permission;
import kasanari.instrumentation.spi.paimon.PaimonCatalogOperation;
import kasanari.server.infrastructure.instrumentation.PaimonCatalogRequestExecutor;

import java.util.Map;
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
    private final PaimonCatalogRequestExecutor executor;

    public PaimonTableServiceHandler(PaimonCatalogRouter paimonCatalogRouter, PaimonCatalogRequestExecutor executor) {
        this.paimonCatalogRouter = paimonCatalogRouter;
        this.executor = executor;
    }

    @Override
    public Response alterTable(String catalogName, String database, String table, AlterTableRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.ALTER_TABLE, Permission.PaimonTableAlter, Map.of("database", database, "table", table),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).alterTable(database, table, request);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response authTableQuery(String catalogName, String database, String table, AuthTableQueryRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.AUTH_TABLE_QUERY, Permission.PaimonTableGet, Map.of("database", database, "table", table),
                () -> {
                    var result = paimonCatalogRouter.getOrThrow(catalogName).authTableQuery(database, table, request);
                    return Response.ok(result).build();
                }
        );
    }

    @Override
    public Response commitTable(String catalogName, String database, String table, CommitTableRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.COMMIT_TABLE, Permission.PaimonTableAlter, Map.of("database", database, "table", table),
                () -> {
                    var result = paimonCatalogRouter.getOrThrow(catalogName).commitTable(database, table, request);
                    return Response.ok(result).build();
                }
        );
    }

    @Override
    public Response createTable(String catalogName, String database, CreateTableRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.CREATE_TABLE, Permission.PaimonTableCreate, Map.of("database", database),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).createTable(database, request);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response dropTable(String catalogName, String database, String table, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.DROP_TABLE, Permission.PaimonTableDrop, Map.of("database", database, "table", table),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).dropTable(database, table);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response getTable(String catalogName, String database, String table, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.GET_TABLE, Permission.PaimonTableGet, Map.of("database", database, "table", table),
                () -> {
                    var tableResp = paimonCatalogRouter.getOrThrow(catalogName).getTable(database, table);
                    return Response.ok(tableResp).build();
                }
        );
    }

    @Override
    public Response getTableById(String catalogName, String tableId, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.GET_TABLE_BY_ID, Permission.PaimonTableGet, Map.of("tableId", tableId),
                () -> {
                    var tableResp = paimonCatalogRouter.getOrThrow(catalogName).getTableById(tableId);
                    return Response.ok(tableResp).build();
                }
        );
    }

    @Override
    public Response getTableSnapshot(String catalogName, String database, String table, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.GET_TABLE_SNAPSHOT, Permission.PaimonTableGet, Map.of("database", database, "table", table),
                () -> {
                    var snapshot = paimonCatalogRouter.getOrThrow(catalogName).getTableSnapshot(database, table);
                    return Response.ok(snapshot).build();
                }
        );
    }

    @Override
    public Response getTableToken(String catalogName, String database, String table, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.GET_TABLE_TOKEN, Permission.PaimonTableGet, Map.of("database", database, "table", table),
                () -> {
                    var token = paimonCatalogRouter.getOrThrow(catalogName).getTableToken(database, table);
                    return Response.ok(token).build();
                }
        );
    }

    @Override
    public Response getVersionSnapshot(String catalogName, String database, String table, String version, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.GET_VERSION_SNAPSHOT, Permission.PaimonTableGet, Map.of("database", database, "table", table, "version", version),
                () -> {
                    var snapshot = paimonCatalogRouter.getOrThrow(catalogName).getVersionSnapshot(database, table, version);
                    return Response.ok(snapshot).build();
                }
        );
    }

    @Override
    public Response listConsumers(String catalogName, String database, String table, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.LIST_CONSUMERS, Permission.PaimonTableList, Map.of("database", database, "table", table),
                () -> {
                    var list = paimonCatalogRouter.getOrThrow(catalogName).listConsumers(database, table, maxResults, pageToken);
                    return Response.ok(list).build();
                }
        );
    }

    @Override
    public Response listSnapshots(String catalogName, String database, String table, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.LIST_SNAPSHOTS, Permission.PaimonTableList, Map.of("database", database, "table", table),
                () -> {
                    var list = paimonCatalogRouter.getOrThrow(catalogName).listSnapshots(database, table, maxResults, pageToken);
                    return Response.ok(list).build();
                }
        );
    }

    @Override
    public Response listTableDetails(String catalogName, String database, Integer maxResults, String pageToken, String tableNamePattern, String tableType, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.LIST_TABLE_DETAILS, Permission.PaimonTableList, Map.of("database", database),
                () -> {
                    var list = paimonCatalogRouter.getOrThrow(catalogName).listTableDetails(database, maxResults, pageToken, tableNamePattern, tableType);
                    return Response.ok(list).build();
                }
        );
    }

    @Override
    public Response listTables(String catalogName, String database, Integer maxResults, String pageToken, String tableNamePattern, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.LIST_TABLES, Permission.PaimonTableList, Map.of("database", database),
                () -> {
                    var list = paimonCatalogRouter.getOrThrow(catalogName).listTables(database, maxResults, pageToken, tableNamePattern);
                    return Response.ok(list).build();
                }
        );
    }

    @Override
    public Response listTablesGlobally(String catalogName, String databaseNamePattern, String tableNamePattern, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.LIST_TABLES_GLOBALLY, Permission.PaimonTableList, Map.of(), () -> {
            var list = paimonCatalogRouter.getOrThrow(catalogName).listTablesGlobally(databaseNamePattern, tableNamePattern, maxResults, pageToken);
            return Response.ok(list).build();
        });
    }

    @Override
    public Response registerTable(String catalogName, String database, RegisterTableRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.REGISTER_TABLE, Permission.PaimonTableAlter, Map.of("database", database),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).registerTable(database, request);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response renameTable(String catalogName, RenameTableRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.RENAME_TABLE, Permission.PaimonTableAlter, Map.of(), () -> {
            paimonCatalogRouter.getOrThrow(catalogName).renameTable(request);
            return Response.ok().build();
        });
    }

    @Override
    public Response resetConsumer(String catalogName, String database, String table, ResetConsumerRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.RESET_CONSUMER, Permission.PaimonTableAlter, Map.of("database", database, "table", table),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).resetConsumer(database, table, request);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response rollbackSchema(String catalogName, String database, String table, RollbackSchemaRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.ROLLBACK_SCHEMA, Permission.PaimonTableAlter, Map.of("database", database, "table", table),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).rollbackSchema(database, table, request);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response rollbackTable(String catalogName, String database, String table, RollbackTableRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.ROLLBACK_TABLE, Permission.PaimonTableAlter, Map.of("database", database, "table", table),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).rollbackTable(database, table, request);
                    return Response.ok().build();
                }
        );
    }
}
