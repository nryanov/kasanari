package kasanari.server.infrastructure.lance;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.lance.api.LanceRestTableService;
import kasanari.authorization.spi.Permission;
import kasanari.instrumentation.spi.lance.LanceCatalogOperation;
import kasanari.server.infrastructure.http.ApiFallbacks;
import kasanari.server.infrastructure.instrumentation.LanceCatalogRequestExecutor;
import org.lance.namespace.model.AlterTableAlterColumnsRequest;
import org.lance.namespace.model.AlterTableDropColumnsRequest;
import org.lance.namespace.model.DeclareTableRequest;
import org.lance.namespace.model.DeregisterTableRequest;
import org.lance.namespace.model.DescribeTableRequest;
import org.lance.namespace.model.RegisterTableRequest;
import org.lance.namespace.model.RenameTableRequest;
import org.lance.namespace.model.RestoreTableRequest;
import org.lance.namespace.model.TableExistsRequest;

import java.io.File;
import java.util.Map;

@ApplicationScoped
public class LanceTableServiceHandler implements LanceRestTableService {
    private final LanceCatalogRequestExecutor executor;

    public LanceTableServiceHandler(LanceCatalogRequestExecutor executor) {
        this.executor = executor;
    }

    @Override
    public Response alterTableAlterColumns(
            String id,
            AlterTableAlterColumnsRequest orgLanceNamespaceModelAlterTableAlterColumnsRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        return executor.execute(securityContext, id, LanceCatalogOperation.ALTER_TABLE_ALTER_COLUMNS, Permission.LanceTableAlter, Map.of(), () ->
                ApiFallbacks.notImplemented("LanceTableService.alterTableAlterColumns"));
    }

    @Override
    public Response alterTableDropColumns(
            String id,
            AlterTableDropColumnsRequest orgLanceNamespaceModelAlterTableDropColumnsRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        return executor.execute(securityContext, id, LanceCatalogOperation.ALTER_TABLE_DROP_COLUMNS, Permission.LanceTableAlter, Map.of(), () ->
                ApiFallbacks.notImplemented("LanceTableService.alterTableDropColumns"));
    }

    @Override
    public Response createTable(
            String id,
            File body,
            String delimiter,
            String mode,
            String properties,
            String storageOptions,
            SecurityContext securityContext
    ) {
        return executor.execute(securityContext, id, LanceCatalogOperation.CREATE_TABLE, Permission.LanceTableCreate, Map.of(), () ->
                ApiFallbacks.notImplemented("LanceTableService.createTable"));
    }

    @Override
    public Response declareTable(
            String id,
            DeclareTableRequest orgLanceNamespaceModelDeclareTableRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        return executor.execute(securityContext, id, LanceCatalogOperation.DECLARE_TABLE, Permission.LanceTableAlter, Map.of(), () ->
                ApiFallbacks.notImplemented("LanceTableService.declareTable"));
    }

    @Override
    public Response deregisterTable(
            String id,
            DeregisterTableRequest orgLanceNamespaceModelDeregisterTableRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        return executor.execute(securityContext, id, LanceCatalogOperation.DEREGISTER_TABLE, Permission.LanceTableAlter, Map.of(), () ->
                ApiFallbacks.notImplemented("LanceTableService.deregisterTable"));
    }

    @Override
    public Response describeTable(
            String id,
            DescribeTableRequest orgLanceNamespaceModelDescribeTableRequest,
            String delimiter,
            Boolean withTableUri,
            Boolean loadDetailedMetadata,
            Boolean checkDeclared,
            SecurityContext securityContext
    ) {
        return executor.execute(securityContext, id, LanceCatalogOperation.DESCRIBE_TABLE, Permission.LanceTableGet, Map.of(), () ->
                ApiFallbacks.notImplemented("LanceTableService.describeTable"));
    }

    @Override
    public Response dropTable(String id, String delimiter, SecurityContext securityContext) {
        return executor.execute(securityContext, id, LanceCatalogOperation.DROP_TABLE, Permission.LanceTableDrop, Map.of(), () ->
                ApiFallbacks.notImplemented("LanceTableService.dropTable"));
    }

    @Override
    public Response registerTable(
            String id,
            RegisterTableRequest orgLanceNamespaceModelRegisterTableRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        return executor.execute(securityContext, id, LanceCatalogOperation.REGISTER_TABLE, Permission.LanceTableAlter, Map.of(), () ->
                ApiFallbacks.notImplemented("LanceTableService.registerTable"));
    }

    @Override
    public Response renameTable(
            String id,
            RenameTableRequest orgLanceNamespaceModelRenameTableRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        return executor.execute(securityContext, id, LanceCatalogOperation.RENAME_TABLE, Permission.LanceTableAlter, Map.of(), () ->
                ApiFallbacks.notImplemented("LanceTableService.renameTable"));
    }

    @Override
    public Response restoreTable(
            String id,
            RestoreTableRequest orgLanceNamespaceModelRestoreTableRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        return executor.execute(securityContext, id, LanceCatalogOperation.RESTORE_TABLE, Permission.LanceTableAlter, Map.of(), () ->
                ApiFallbacks.notImplemented("LanceTableService.restoreTable"));
    }

    @Override
    public Response tableExists(
            String id,
            TableExistsRequest orgLanceNamespaceModelTableExistsRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        return executor.execute(securityContext, id, LanceCatalogOperation.TABLE_EXISTS, Permission.LanceTableExists, Map.of(), () ->
                ApiFallbacks.notImplemented("LanceTableService.tableExists"));
    }
}
