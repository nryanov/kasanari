package kasanari.server.infrastructure.paimon;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestTableService;
import kasanari.server.infrastructure.http.ApiFallbacks;
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
    @Override
    public Response alterTable(String prefix, String database, String table, AlterTableRequest orgApachePaimonRestRequestsAlterTableRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.alterTable");
    }

    @Override
    public Response authTableQuery(String prefix, String database, String table, AuthTableQueryRequest orgApachePaimonRestRequestsAuthTableQueryRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.authTableQuery");
    }

    @Override
    public Response commitTable(String prefix, String database, String table, CommitTableRequest orgApachePaimonRestRequestsCommitTableRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.commitTable");
    }

    @Override
    public Response createTable(String prefix, String database, CreateTableRequest orgApachePaimonRestRequestsCreateTableRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.createTable");
    }

    @Override
    public Response dropTable(String prefix, String database, String table, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.dropTable");
    }

    @Override
    public Response getTable(String prefix, String database, String table, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.getTable");
    }

    @Override
    public Response getTableById(String prefix, String tableId, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.getTableById");
    }

    @Override
    public Response getTableSnapshot(String prefix, String database, String table, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.getTableSnapshot");
    }

    @Override
    public Response getTableToken(String prefix, String database, String table, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.getTableToken");
    }

    @Override
    public Response getVersionSnapshot(String prefix, String database, String table, String version, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.getVersionSnapshot");
    }

    @Override
    public Response listConsumers(String prefix, String database, String table, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.listConsumers");
    }

    @Override
    public Response listSnapshots(String prefix, String database, String table, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.listSnapshots");
    }

    @Override
    public Response listTableDetails(String prefix, String database, Integer maxResults, String pageToken, String tableNamePattern, String tableType, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.listTableDetails");
    }

    @Override
    public Response listTables(String prefix, String database, Integer maxResults, String pageToken, String tableNamePattern, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.listTables");
    }

    @Override
    public Response listTablesGlobally(String prefix, String databaseNamePattern, String tableNamePattern, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.listTablesGlobally");
    }

    @Override
    public Response registerTable(String prefix, String database, RegisterTableRequest orgApachePaimonRestRequestsRegisterTableRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.registerTable");
    }

    @Override
    public Response renameTable(String prefix, RenameTableRequest orgApachePaimonRestRequestsRenameTableRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.renameTable");
    }

    @Override
    public Response resetConsumer(String prefix, String database, String table, ResetConsumerRequest orgApachePaimonRestRequestsResetConsumerRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.resetConsumer");
    }

    @Override
    public Response rollbackSchema(String prefix, String database, String table, RollbackSchemaRequest orgApachePaimonRestRequestsRollbackSchemaRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.rollbackSchema");
    }

    @Override
    public Response rollbackTable(String prefix, String database, String table, RollbackTableRequest orgApachePaimonRestRequestsRollbackTableRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonTableService.rollbackTable");
    }
}
