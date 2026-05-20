package kasanari.server.infrastructure.paimon;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.repository.management.common.model.CatalogType;
import kasanari.server.infrastructure.security.CatalogHandlerAuthorization;

import java.util.Optional;

abstract class PaimonAuthorizedHandler {
    private static final CatalogType DOMAIN = CatalogType.PAIMON;

    private final AuthorizationService authorizationService;

    protected PaimonAuthorizedHandler(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    protected Optional<Response> deny(SecurityContext securityContext, Permission permission) {
        return CatalogHandlerAuthorization.denyUnless(authorizationService, securityContext, DOMAIN, permission);
    }
}
