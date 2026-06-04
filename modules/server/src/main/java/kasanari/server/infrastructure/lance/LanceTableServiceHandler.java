package kasanari.server.infrastructure.lance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.lance.api.LanceRestTableService;
import kasanari.authorization.spi.Permission;
import kasanari.instrumentation.spi.lance.LanceCatalogOperation;
import kasanari.server.infrastructure.instrumentation.LanceCatalogRequestExecutor;
import org.lance.namespace.model.AlterTableAlterColumnsRequest;
import org.lance.namespace.model.CreateTableRequest;
import org.lance.namespace.model.AlterTableDropColumnsRequest;
import org.lance.namespace.model.DeclareTableRequest;
import org.lance.namespace.model.DeregisterTableRequest;
import org.lance.namespace.model.DescribeTableRequest;
import org.lance.namespace.model.DropTableRequest;
import org.lance.namespace.model.RegisterTableRequest;
import org.lance.namespace.model.RenameTableRequest;
import org.lance.namespace.model.RestoreTableRequest;
import org.lance.namespace.model.TableExistsRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import static kasanari.server.infrastructure.lance.LanceCatalogHelper.parseCatalogNamespaceTableId;

@ApplicationScoped
public class LanceTableServiceHandler implements LanceRestTableService {
    private final LanceCatalogRequestExecutor executor;
    private final LanceCatalogRouter catalogRouter;
    private final ObjectMapper objectMapper;

    public LanceTableServiceHandler(LanceCatalogRequestExecutor executor, LanceCatalogRouter catalogRouter, ObjectMapper objectMapper) {
        this.executor = executor;
        this.catalogRouter = catalogRouter;
        this.objectMapper = objectMapper;
    }

    @Override
    public Response alterTableAlterColumns(
            String id,
            AlterTableAlterColumnsRequest orgLanceNamespaceModelAlterTableAlterColumnsRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        var parsedId = parseCatalogNamespaceTableId(id, delimiter);
        orgLanceNamespaceModelAlterTableAlterColumnsRequest.id(List.of(parsedId.namespace(), parsedId.table()));

        return executor.execute(
                securityContext,
                parsedId.catalog(),
                LanceCatalogOperation.ALTER_TABLE_ALTER_COLUMNS,
                Permission.LanceTableAlter,
                Map.of("namespace", parsedId.namespace(), "table", parsedId.table()),
                () -> Response.ok(catalogRouter.getOrThrow(parsedId.catalog()).alterTableAlterColumns(orgLanceNamespaceModelAlterTableAlterColumnsRequest)).build()
        );
    }

    @Override
    public Response alterTableDropColumns(
            String id,
            AlterTableDropColumnsRequest orgLanceNamespaceModelAlterTableDropColumnsRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        var parsedId = parseCatalogNamespaceTableId(id, delimiter);
        orgLanceNamespaceModelAlterTableDropColumnsRequest.id(List.of(parsedId.namespace(), parsedId.table()));

        return executor.execute(
                securityContext,
                parsedId.catalog(),
                LanceCatalogOperation.ALTER_TABLE_DROP_COLUMNS,
                Permission.LanceTableAlter,
                Map.of("namespace", parsedId.namespace(), "table", parsedId.table()),
                () -> Response.ok(catalogRouter.getOrThrow(parsedId.catalog()).alterTableDropColumns(orgLanceNamespaceModelAlterTableDropColumnsRequest)).build()
        );
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
        var parsedId = parseCatalogNamespaceTableId(id, delimiter);
        var request = new CreateTableRequest()
                .id(List.of(parsedId.namespace(), parsedId.table()))
                .mode(mode)
                .properties(parseJsonMap(properties, "properties"))
                .storageOptions(parseJsonMap(storageOptions, "storageOptions"));

        return executor.execute(
                securityContext,
                parsedId.catalog(),
                LanceCatalogOperation.CREATE_TABLE,
                Permission.LanceTableCreate,
                Map.of("namespace", parsedId.namespace(), "table", parsedId.table()),
                () -> Response.ok(catalogRouter.getOrThrow(parsedId.catalog()).createTable(request, readRequestBody(body))).build()
        );
    }

    @Override
    public Response declareTable(
            String id,
            DeclareTableRequest orgLanceNamespaceModelDeclareTableRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        var parsedId = parseCatalogNamespaceTableId(id, delimiter);
        orgLanceNamespaceModelDeclareTableRequest.id(List.of(parsedId.namespace(), parsedId.table()));

        return executor.execute(
                securityContext,
                parsedId.catalog(),
                LanceCatalogOperation.DECLARE_TABLE,
                Permission.LanceTableAlter,
                Map.of("namespace", parsedId.namespace(), "table", parsedId.table()),
                () -> Response.ok(catalogRouter.getOrThrow(parsedId.catalog()).createEmptyTable(orgLanceNamespaceModelDeclareTableRequest)).build()
        );
    }

    @Override
    public Response deregisterTable(
            String id,
            DeregisterTableRequest orgLanceNamespaceModelDeregisterTableRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        var parsedId = parseCatalogNamespaceTableId(id, delimiter);
        orgLanceNamespaceModelDeregisterTableRequest.id(List.of(parsedId.namespace(), parsedId.table()));

        return executor.execute(
                securityContext,
                parsedId.catalog(),
                LanceCatalogOperation.DEREGISTER_TABLE,
                Permission.LanceTableAlter,
                Map.of("namespace", parsedId.namespace(), "table", parsedId.table()),
                () -> Response.ok(catalogRouter.getOrThrow(parsedId.catalog()).deregisterTable(orgLanceNamespaceModelDeregisterTableRequest)).build()
        );
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
        var parsedId = parseCatalogNamespaceTableId(id, delimiter);
        orgLanceNamespaceModelDescribeTableRequest.id(List.of(parsedId.namespace(), parsedId.table()));

        return executor.execute(
                securityContext,
                parsedId.catalog(),
                LanceCatalogOperation.DESCRIBE_TABLE,
                Permission.LanceTableGet,
                Map.of("namespace", parsedId.namespace(), "table", parsedId.table()),
                () -> Response.ok(catalogRouter.getOrThrow(parsedId.catalog()).describeTable(orgLanceNamespaceModelDescribeTableRequest)).build()
        );
    }

    @Override
    public Response dropTable(String id, String delimiter, SecurityContext securityContext) {
        var parsedId = parseCatalogNamespaceTableId(id, delimiter);
        var request = new DropTableRequest().id(List.of(parsedId.namespace(), parsedId.table()));

        return executor.execute(
                securityContext,
                parsedId.catalog(),
                LanceCatalogOperation.DROP_TABLE,
                Permission.LanceTableDrop,
                Map.of("namespace", parsedId.namespace(), "table", parsedId.table()),
                () -> Response.ok(catalogRouter.getOrThrow(parsedId.catalog()).dropTable(request)).build()
        );
    }

    @Override
    public Response registerTable(
            String id,
            RegisterTableRequest orgLanceNamespaceModelRegisterTableRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        var parsedId = parseCatalogNamespaceTableId(id, delimiter);
        orgLanceNamespaceModelRegisterTableRequest.id(List.of(parsedId.namespace(), parsedId.table()));

        return executor.execute(
                securityContext,
                parsedId.catalog(),
                LanceCatalogOperation.REGISTER_TABLE,
                Permission.LanceTableAlter,
                Map.of("namespace", parsedId.namespace(), "table", parsedId.table()),
                () -> Response.ok(catalogRouter.getOrThrow(parsedId.catalog()).registerTable(orgLanceNamespaceModelRegisterTableRequest)).build()
        );
    }

    @Override
    public Response renameTable(
            String id,
            RenameTableRequest orgLanceNamespaceModelRenameTableRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        var parsedId = parseCatalogNamespaceTableId(id, delimiter);
        orgLanceNamespaceModelRenameTableRequest.id(List.of(parsedId.namespace(), parsedId.table()));

        return executor.execute(
                securityContext,
                parsedId.catalog(),
                LanceCatalogOperation.RENAME_TABLE,
                Permission.LanceTableAlter,
                Map.of("namespace", parsedId.namespace(), "table", parsedId.table()),
                () -> Response.ok(catalogRouter.getOrThrow(parsedId.catalog()).renameTable(orgLanceNamespaceModelRenameTableRequest)).build()
        );
    }

    @Override
    public Response restoreTable(
            String id,
            RestoreTableRequest orgLanceNamespaceModelRestoreTableRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        var parsedId = parseCatalogNamespaceTableId(id, delimiter);
        orgLanceNamespaceModelRestoreTableRequest.id(List.of(parsedId.namespace(), parsedId.table()));

        return executor.execute(
                securityContext,
                parsedId.catalog(),
                LanceCatalogOperation.RESTORE_TABLE,
                Permission.LanceTableAlter,
                Map.of("namespace", parsedId.namespace(), "table", parsedId.table()),
                () -> Response.ok(catalogRouter.getOrThrow(parsedId.catalog()).restoreTable(orgLanceNamespaceModelRestoreTableRequest)).build()
        );
    }

    @Override
    public Response tableExists(
            String id,
            TableExistsRequest orgLanceNamespaceModelTableExistsRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        var parsedId = parseCatalogNamespaceTableId(id, delimiter);
        orgLanceNamespaceModelTableExistsRequest.id(List.of(parsedId.namespace(), parsedId.table()));

        return executor.execute(
                securityContext,
                parsedId.catalog(),
                LanceCatalogOperation.TABLE_EXISTS,
                Permission.LanceTableExists,
                Map.of("namespace", parsedId.namespace(), "table", parsedId.table()),
                () -> {
                    catalogRouter.getOrThrow(parsedId.catalog()).tableExists(orgLanceNamespaceModelTableExistsRequest);
                    return Response.ok().build();
                }
        );
    }

    private Map<String, String> parseJsonMap(String value, String paramName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() { });
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid JSON map in query parameter '" + paramName + "'", e);
        }
    }

    private byte[] readRequestBody(File body) {
        if (body == null) {
            return new byte[0];
        }
        try {
            return Files.readAllBytes(body.toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read request body", e);
        }
    }
}
