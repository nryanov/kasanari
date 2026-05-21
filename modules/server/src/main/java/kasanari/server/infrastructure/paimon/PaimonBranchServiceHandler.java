package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestBranchService;
import kasanari.authorization.spi.Permission;
import kasanari.instrumentation.spi.paimon.PaimonCatalogOperation;
import kasanari.server.infrastructure.instrumentation.PaimonCatalogRequestExecutor;

import java.util.Map;
import org.apache.paimon.rest.requests.CreateBranchRequest;
import org.apache.paimon.rest.requests.ForwardBranchRequest;
import org.apache.paimon.rest.requests.RenameBranchRequest;

@ApplicationScoped
public class PaimonBranchServiceHandler implements PaimonRestBranchService {
    private final PaimonCatalogRouter paimonCatalogRouter;
    private final PaimonCatalogRequestExecutor executor;

    public PaimonBranchServiceHandler(PaimonCatalogRouter paimonCatalogRouter, PaimonCatalogRequestExecutor executor) {
        this.paimonCatalogRouter = paimonCatalogRouter;
        this.executor = executor;
    }

    @Override
    public Response createBranch(String catalogName, String database, String table, CreateBranchRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.CREATE_BRANCH, Permission.PaimonBranchCreate, Map.of("database", database, "table", table),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).createBranch(database, table, request);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response dropBranch(String catalogName, String database, String table, String branch, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.DROP_BRANCH, Permission.PaimonBranchDrop, Map.of("database", database, "table", table, "branch", branch),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).dropBranch(database, table, branch);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response forwardBranch(String catalogName, String database, String table, String branch, ForwardBranchRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.FORWARD_BRANCH, Permission.PaimonBranchAlter, Map.of("database", database, "table", table, "branch", branch),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).forwardBranch(database, table, branch, request);
                    return Response.ok().build();
                }
        );
    }

    @Override
    public Response listBranches(String catalogName, String database, String table, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.LIST_BRANCHES, Permission.PaimonBranchList, Map.of("database", database, "table", table),
                () -> {
                    var list = paimonCatalogRouter.getOrThrow(catalogName).listBranches(database, table);
                    return Response.ok(list).build();
                }
        );
    }

    @Override
    public Response renameBranch(String catalogName, String database, String table, String branch, RenameBranchRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.RENAME_BRANCH, Permission.PaimonBranchAlter, Map.of("database", database, "table", table, "branch", branch),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).renameBranch(database, table, branch, request);
                    return Response.ok().build();
                }
        );
    }
}
