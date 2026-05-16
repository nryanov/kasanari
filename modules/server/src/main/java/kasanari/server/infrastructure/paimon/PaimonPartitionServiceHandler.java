package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestPartitionService;
import org.apache.paimon.rest.requests.ListPartitionsByNamesRequest;
import org.apache.paimon.rest.requests.MarkDonePartitionsRequest;

@ApplicationScoped
public class PaimonPartitionServiceHandler implements PaimonRestPartitionService {
    private final PaimonCatalogRouter paimonCatalogRouter;

    public PaimonPartitionServiceHandler(PaimonCatalogRouter paimonCatalogRouter) {
        this.paimonCatalogRouter = paimonCatalogRouter;
    }

    @Override
    public Response listPartitions(String catalogName, String database, String table, Integer maxResults, String pageToken, String partitionNamePattern, SecurityContext securityContext) {
        var list = paimonCatalogRouter.getOrThrow(catalogName).listPartitions(database, table, maxResults, pageToken, partitionNamePattern);
        return Response.ok(list).build();
    }

    @Override
    public Response listPartitionsByNames(String catalogName, String database, String table, ListPartitionsByNamesRequest request, SecurityContext securityContext) {
        var list = paimonCatalogRouter.getOrThrow(catalogName).listPartitionsByNames(database, table, request);
        return Response.ok(list).build();
    }

    @Override
    public Response markDonePartitions(String catalogName, String database, String table, MarkDonePartitionsRequest request, SecurityContext securityContext) {
        paimonCatalogRouter.getOrThrow(catalogName).markDonePartitions(database, table, request);
        return Response.ok().build();
    }
}
