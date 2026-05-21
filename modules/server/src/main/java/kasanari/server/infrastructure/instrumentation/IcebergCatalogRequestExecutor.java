package kasanari.server.infrastructure.instrumentation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.core.model.CatalogType;
import kasanari.instrumentation.runtime.CatalogRequestInstrumentation;
import kasanari.instrumentation.spi.iceberg.IcebergCatalogOperation;
import kasanari.instrumentation.spi.iceberg.IcebergCatalogRequestContext;

import java.util.Map;
import java.util.function.Supplier;

@ApplicationScoped
public class IcebergCatalogRequestExecutor {
    private static final CatalogType DOMAIN = CatalogType.ICEBERG;

    private final AuthorizationService authorizationService;
    private final CatalogRequestInstrumentation instrumentation;

    @Inject
    public IcebergCatalogRequestExecutor(
            AuthorizationService authorizationService,
            CatalogRequestInstrumentation instrumentation
    ) {
        this.authorizationService = authorizationService;
        this.instrumentation = instrumentation;
    }

    public Response execute(
            SecurityContext securityContext,
            String catalogName,
            IcebergCatalogOperation operation,
            Permission permission,
            Map<String, String> attributes,
            Supplier<Response> action
    ) {
        var subject = authorizationService.subject(securityContext);
        var ctx = new IcebergCatalogRequestContext(catalogName, operation, subject, attributes);
        var denied = authorizationService.denyUnless(securityContext, DOMAIN, permission);
        if (denied.isPresent()) {
            instrumentation.icebergPipeline().notifyDenied(ctx);
            return denied.get();
        }
        return instrumentation.icebergPipeline().execute(ctx, action);
    }
}
