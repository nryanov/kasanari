package kasanari.server.paimon;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestPartitionService;
import kasanari.server.http.ApiFallbacks;
import org.apache.paimon.rest.requests.ListPartitionsByNamesRequest;
import org.apache.paimon.rest.requests.MarkDonePartitionsRequest;

@ApplicationScoped
public class PaimonPartitionService implements PaimonRestPartitionService {
    @Override
    public Response listPartitions(String prefix, String database, String table, Integer maxResults, String pageToken, String partitionNamePattern, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonPartitionService.listPartitions");
    }

    @Override
    public Response listPartitionsByNames(String prefix, String database, String table, ListPartitionsByNamesRequest orgApachePaimonRestRequestsListPartitionsByNamesRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonPartitionService.listPartitionsByNames");
    }

    @Override
    public Response markDonePartitions(String prefix, String database, String table, MarkDonePartitionsRequest orgApachePaimonRestRequestsMarkDonePartitionsRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonPartitionService.markDonePartitions");
    }
}
