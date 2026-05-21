package kasanari.server.infrastructure.security;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.core.model.CatalogType;

import java.util.Optional;

public final class CatalogHandlerAuthorization {
    private CatalogHandlerAuthorization() {
    }

    public static Optional<Response> denyUnless(
            AuthorizationService authorizationService,
            SecurityContext securityContext,
            CatalogType domain,
            Permission permission
    ) {
        return authorizationService.denyUnless(securityContext, domain, permission);
    }
}
