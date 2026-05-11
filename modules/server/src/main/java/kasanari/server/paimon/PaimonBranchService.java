package kasanari.server.paimon;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestBranchService;
import kasanari.server.http.ApiFallbacks;
import org.apache.paimon.rest.requests.CreateBranchRequest;
import org.apache.paimon.rest.requests.ForwardBranchRequest;
import org.apache.paimon.rest.requests.RenameBranchRequest;

@ApplicationScoped
public class PaimonBranchService implements PaimonRestBranchService {
    @Override
    public Response createBranch(String prefix, String database, String table, CreateBranchRequest orgApachePaimonRestRequestsCreateBranchRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonBranchService.createBranch");
    }

    @Override
    public Response dropBranch(String prefix, String database, String table, String branch, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonBranchService.dropBranch");
    }

    @Override
    public Response forwardBranch(String prefix, String database, String table, String branch, ForwardBranchRequest orgApachePaimonRestRequestsForwardBranchRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonBranchService.forwardBranch");
    }

    @Override
    public Response listBranches(String prefix, String database, String table, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonBranchService.listBranches");
    }

    @Override
    public Response renameBranch(String prefix, String database, String table, String branch, RenameBranchRequest orgApachePaimonRestRequestsRenameBranchRequest, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonBranchService.renameBranch");
    }
}
