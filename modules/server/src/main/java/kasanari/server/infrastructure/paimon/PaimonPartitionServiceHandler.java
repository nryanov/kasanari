package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestPartitionService;
import kasanari.authorization.spi.Permission;
import kasanari.instrumentation.spi.paimon.PaimonCatalogOperation;
import kasanari.server.infrastructure.instrumentation.PaimonCatalogRequestExecutor;

import java.util.Map;
import org.apache.paimon.rest.requests.ListPartitionsByNamesRequest;
import org.apache.paimon.rest.requests.MarkDonePartitionsRequest;

@ApplicationScoped
public class PaimonPartitionServiceHandler implements PaimonRestPartitionService {
    private final PaimonCatalogRouter paimonCatalogRouter;
    private final PaimonCatalogRequestExecutor executor;

    public PaimonPartitionServiceHandler(PaimonCatalogRouter paimonCatalogRouter, PaimonCatalogRequestExecutor executor) {
        this.paimonCatalogRouter = paimonCatalogRouter;
        this.executor = executor;
    }

    @Override
    public Response listPartitions(String catalogName, String database, String table, Integer maxResults, String pageToken, String partitionNamePattern, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.LIST_PARTITIONS, Permission.PaimonPartitionList, Map.of("database", database, "table", table),
                () -> {
                    var list = paimonCatalogRouter.getOrThrow(catalogName).listPartitions(database, table, maxResults, pageToken, partitionNamePattern);
                    return Response.ok(list).build();
                }
        );
    }

    @Override
    public Response listPartitionsByNames(String catalogName, String database, String table, ListPartitionsByNamesRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.LIST_PARTITIONS_BY_NAMES, Permission.PaimonPartitionList, Map.of("database", database, "table", table),
                () -> {
                    var list = paimonCatalogRouter.getOrThrow(catalogName).listPartitionsByNames(database, table, request);
                    return Response.ok(list).build();
                }
        );
    }

    @Override
    public Response markDonePartitions(String catalogName, String database, String table, MarkDonePartitionsRequest request, SecurityContext securityContext) {
        return executor.execute(securityContext, catalogName, PaimonCatalogOperation.MARK_DONE_PARTITIONS, Permission.PaimonPartitionAlter, Map.of("database", database, "table", table),
                () -> {
                    paimonCatalogRouter.getOrThrow(catalogName).markDonePartitions(database, table, request);
                    return Response.ok().build();
                }
        );
    }
}
