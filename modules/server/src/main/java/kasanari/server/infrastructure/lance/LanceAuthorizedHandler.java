package kasanari.server.infrastructure.lance;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.repository.management.common.model.CatalogType;
import kasanari.server.infrastructure.security.CatalogHandlerAuthorization;

import java.util.Optional;

abstract class LanceAuthorizedHandler {
    private static final CatalogType DOMAIN = CatalogType.LANCE;

    private final AuthorizationService authorizationService;

    protected LanceAuthorizedHandler(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    protected Optional<Response> deny(SecurityContext securityContext, Permission permission) {
        return CatalogHandlerAuthorization.denyUnless(authorizationService, securityContext, DOMAIN, permission);
    }
}
