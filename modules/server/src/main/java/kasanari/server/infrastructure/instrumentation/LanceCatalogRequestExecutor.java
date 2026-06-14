package kasanari.server.infrastructure.instrumentation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.core.model.CatalogType;
import kasanari.instrumentation.spi.lance.LanceCatalogOperation;
import kasanari.instrumentation.spi.lance.LanceCatalogRequestContext;

import java.util.Map;
import java.util.function.Supplier;

@ApplicationScoped
public class LanceCatalogRequestExecutor {
    private static final CatalogType DOMAIN = CatalogType.LANCE;

    private final AuthorizationService authorizationService;
    private final CatalogRequestInstrumentation instrumentation;

    @Inject
    public LanceCatalogRequestExecutor(
            AuthorizationService authorizationService,
            CatalogRequestInstrumentation instrumentation
    ) {
        this.authorizationService = authorizationService;
        this.instrumentation = instrumentation;
    }

    public Response execute(
            SecurityContext securityContext,
            String catalogName,
            LanceCatalogOperation operation,
            Permission permission,
            Map<String, String> attributes,
            Supplier<Response> action
    ) {
        var subject = authorizationService.subject(securityContext);
        var ctx = new LanceCatalogRequestContext(catalogName, operation, subject, attributes);
        var denied = authorizationService.denyUnless(securityContext, DOMAIN, permission);
        if (denied.isPresent()) {
            instrumentation.lancePipeline().notifyDenied(ctx);
            return denied.get();
        }
        return instrumentation.lancePipeline().execute(ctx, action);
    }
}
