package kasanari.server.infrastructure.lance;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.catalog.lance.api.LanceRestTableService;
import kasanari.repository.management.common.model.CatalogType;
import kasanari.server.infrastructure.http.ApiFallbacks;
import kasanari.server.infrastructure.security.CatalogHandlerAuthorization;
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
import java.util.Optional;

@ApplicationScoped
public class LanceTableServiceHandler implements LanceRestTableService {
    private static final CatalogType DOMAIN = CatalogType.LANCE;

    private final AuthorizationService authorizationService;

    public LanceTableServiceHandler(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public Response alterTableAlterColumns(
            String id,
            AlterTableAlterColumnsRequest orgLanceNamespaceModelAlterTableAlterColumnsRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        var denied = deny(securityContext, Permission.LanceTableAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ApiFallbacks.notImplemented("LanceTableService.alterTableAlterColumns");
    }

    @Override
    public Response alterTableDropColumns(
            String id,
            AlterTableDropColumnsRequest orgLanceNamespaceModelAlterTableDropColumnsRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        var denied = deny(securityContext, Permission.LanceTableAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ApiFallbacks.notImplemented("LanceTableService.alterTableDropColumns");
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
        var denied = deny(securityContext, Permission.LanceTableCreate);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ApiFallbacks.notImplemented("LanceTableService.createTable");
    }

    @Override
    public Response declareTable(
            String id,
            DeclareTableRequest orgLanceNamespaceModelDeclareTableRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        var denied = deny(securityContext, Permission.LanceTableAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ApiFallbacks.notImplemented("LanceTableService.declareTable");
    }

    @Override
    public Response deregisterTable(
            String id,
            DeregisterTableRequest orgLanceNamespaceModelDeregisterTableRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        var denied = deny(securityContext, Permission.LanceTableAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ApiFallbacks.notImplemented("LanceTableService.deregisterTable");
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
        var denied = deny(securityContext, Permission.LanceTableGet);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ApiFallbacks.notImplemented("LanceTableService.describeTable");
    }

    @Override
    public Response dropTable(String id, String delimiter, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.LanceTableDrop);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ApiFallbacks.notImplemented("LanceTableService.dropTable");
    }

    @Override
    public Response registerTable(
            String id,
            RegisterTableRequest orgLanceNamespaceModelRegisterTableRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        var denied = deny(securityContext, Permission.LanceTableAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ApiFallbacks.notImplemented("LanceTableService.registerTable");
    }

    @Override
    public Response renameTable(
            String id,
            RenameTableRequest orgLanceNamespaceModelRenameTableRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        var denied = deny(securityContext, Permission.LanceTableAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ApiFallbacks.notImplemented("LanceTableService.renameTable");
    }

    @Override
    public Response restoreTable(
            String id,
            RestoreTableRequest orgLanceNamespaceModelRestoreTableRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        var denied = deny(securityContext, Permission.LanceTableAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ApiFallbacks.notImplemented("LanceTableService.restoreTable");
    }

    @Override
    public Response tableExists(
            String id,
            TableExistsRequest orgLanceNamespaceModelTableExistsRequest,
            String delimiter,
            SecurityContext securityContext
    ) {
        var denied = deny(securityContext, Permission.LanceTableExists);
        if (denied.isPresent()) {
            return denied.get();
        }
        return ApiFallbacks.notImplemented("LanceTableService.tableExists");
    }

    private Optional<Response> deny(SecurityContext securityContext, Permission permission) {
        return CatalogHandlerAuthorization.denyUnless(authorizationService, securityContext, DOMAIN, permission);
    }
}
