package kasanari.server.infrastructure.instrumentation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.core.model.CatalogType;
import kasanari.instrumentation.spi.paimon.PaimonCatalogOperation;
import kasanari.instrumentation.spi.paimon.PaimonCatalogRequestContext;
import kasanari.server.infrastructure.authorization.AuthorizationResourceResolver;

import java.util.Map;
import java.util.function.Supplier;

@ApplicationScoped
public class PaimonCatalogRequestExecutor {
    private static final CatalogType DOMAIN = CatalogType.PAIMON;

    private final AuthorizationService authorizationService;
    private final CatalogRequestInstrumentation instrumentation;

    @Inject
    public PaimonCatalogRequestExecutor(
            AuthorizationService authorizationService,
            CatalogRequestInstrumentation instrumentation
    ) {
        this.authorizationService = authorizationService;
        this.instrumentation = instrumentation;
    }

    public Response execute(
            SecurityContext securityContext,
            String catalogName,
            PaimonCatalogOperation operation,
            Permission permission,
            Map<String, String> attributes,
            Supplier<Response> action
    ) {
        var subject = authorizationService.subject(securityContext);
        var ctx = new PaimonCatalogRequestContext(catalogName, operation, subject, attributes);
        var resource = AuthorizationResourceResolver.resolve(DOMAIN, catalogName, attributes);
        var denied = authorizationService.denyUnless(securityContext, resource, permission);
        if (denied.isPresent()) {
            instrumentation.paimonPipeline().notifyDenied(ctx);
            return denied.get();
        }
        return instrumentation.paimonPipeline().execute(ctx, action);
    }
}
