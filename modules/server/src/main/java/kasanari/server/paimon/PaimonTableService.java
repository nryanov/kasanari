package kasanari.server.paimon;


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
public class PaimonTableService implements PaimonRestTableService {
    @Override
    public Response alterTable(String prefix, String database, String table, AlterTableRequest orgApachePaimonRestRequestsAlterTableRequest, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response authTableQuery(String prefix, String database, String table, AuthTableQueryRequest orgApachePaimonRestRequestsAuthTableQueryRequest, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response commitTable(String prefix, String database, String table, CommitTableRequest orgApachePaimonRestRequestsCommitTableRequest, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response createTable(String prefix, String database, CreateTableRequest orgApachePaimonRestRequestsCreateTableRequest, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response dropTable(String prefix, String database, String table, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response getTable(String prefix, String database, String table, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response getTableById(String prefix, String tableId, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response getTableSnapshot(String prefix, String database, String table, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response getTableToken(String prefix, String database, String table, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response getVersionSnapshot(String prefix, String database, String table, String version, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response listConsumers(String prefix, String database, String table, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response listSnapshots(String prefix, String database, String table, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response listTableDetails(String prefix, String database, Integer maxResults, String pageToken, String tableNamePattern, String tableType, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response listTables(String prefix, String database, Integer maxResults, String pageToken, String tableNamePattern, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response listTablesGlobally(String prefix, String databaseNamePattern, String tableNamePattern, Integer maxResults, String pageToken, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response registerTable(String prefix, String database, RegisterTableRequest orgApachePaimonRestRequestsRegisterTableRequest, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response renameTable(String prefix, RenameTableRequest orgApachePaimonRestRequestsRenameTableRequest, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response resetConsumer(String prefix, String database, String table, ResetConsumerRequest orgApachePaimonRestRequestsResetConsumerRequest, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response rollbackSchema(String prefix, String database, String table, RollbackSchemaRequest orgApachePaimonRestRequestsRollbackSchemaRequest, SecurityContext securityContext) {
        return null;
    }

    @Override
    public Response rollbackTable(String prefix, String database, String table, RollbackTableRequest orgApachePaimonRestRequestsRollbackTableRequest, SecurityContext securityContext) {
        return null;
    }
}
