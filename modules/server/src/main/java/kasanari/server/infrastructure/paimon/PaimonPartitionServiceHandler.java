package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.catalog.paimon.api.PaimonRestPartitionService;
import kasanari.repository.management.common.model.CatalogType;
import kasanari.server.infrastructure.security.CatalogHandlerAuthorization;

import java.util.Optional;
import org.apache.paimon.rest.requests.ListPartitionsByNamesRequest;
import org.apache.paimon.rest.requests.MarkDonePartitionsRequest;

@ApplicationScoped
public class PaimonPartitionServiceHandler implements PaimonRestPartitionService {
    private static final CatalogType DOMAIN = CatalogType.PAIMON;

    private final PaimonCatalogRouter paimonCatalogRouter;
    private final AuthorizationService authorizationService;

    public PaimonPartitionServiceHandler(PaimonCatalogRouter paimonCatalogRouter, AuthorizationService authorizationService) {
        this.paimonCatalogRouter = paimonCatalogRouter;
        this.authorizationService = authorizationService;
    }

    @Override
    public Response listPartitions(String catalogName, String database, String table, Integer maxResults, String pageToken, String partitionNamePattern, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonPartitionList);
        if (denied.isPresent()) {
            return denied.get();
        }
        var list = paimonCatalogRouter.getOrThrow(catalogName).listPartitions(database, table, maxResults, pageToken, partitionNamePattern);
        return Response.ok(list).build();
    }

    @Override
    public Response listPartitionsByNames(String catalogName, String database, String table, ListPartitionsByNamesRequest request, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonPartitionList);
        if (denied.isPresent()) {
            return denied.get();
        }
        var list = paimonCatalogRouter.getOrThrow(catalogName).listPartitionsByNames(database, table, request);
        return Response.ok(list).build();
    }

    @Override
    public Response markDonePartitions(String catalogName, String database, String table, MarkDonePartitionsRequest request, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonPartitionAlter);
        if (denied.isPresent()) {
            return denied.get();
        }
        paimonCatalogRouter.getOrThrow(catalogName).markDonePartitions(database, table, request);
        return Response.ok().build();
    }

    private Optional<Response> deny(SecurityContext securityContext, Permission permission) {
        return CatalogHandlerAuthorization.denyUnless(authorizationService, securityContext, DOMAIN, permission);
    }
}
