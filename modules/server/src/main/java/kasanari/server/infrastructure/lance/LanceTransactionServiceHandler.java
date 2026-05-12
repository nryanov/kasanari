package kasanari.server.infrastructure.lance;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.lance.api.LanceRestTransactionService;
import kasanari.server.infrastructure.http.ApiFallbacks;
import org.lance.namespace.model.AlterTransactionRequest;
import org.lance.namespace.model.DescribeTransactionRequest;

@ApplicationScoped
public class LanceTransactionServiceHandler implements LanceRestTransactionService {
    @Override
    public Response alterTransaction(String id, AlterTransactionRequest orgLanceNamespaceModelAlterTransactionRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTransactionService.alterTransaction");
    }

    @Override
    public Response describeTransaction(String id, DescribeTransactionRequest orgLanceNamespaceModelDescribeTransactionRequest, String delimiter, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("LanceTransactionService.describeTransaction");
    }
}
