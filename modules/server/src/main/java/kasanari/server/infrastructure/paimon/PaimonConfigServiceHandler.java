package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.catalog.paimon.api.PaimonRestConfigService;
import kasanari.repository.management.common.model.CatalogType;
import kasanari.server.infrastructure.security.CatalogHandlerAuthorization;
import org.apache.paimon.rest.responses.ConfigResponse;

import java.util.HashMap;
import java.util.Optional;

@ApplicationScoped
public class PaimonConfigServiceHandler implements PaimonRestConfigService {
    private static final CatalogType DOMAIN = CatalogType.PAIMON;

    private final AuthorizationService authorizationService;

    public PaimonConfigServiceHandler(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public Response getConfig(String warehouse, SecurityContext securityContext) {
        var denied = deny(securityContext, Permission.PaimonConfigGet);
        if (denied.isPresent()) {
            return denied.get();
        }
        var defaults = new HashMap<String, String>();
        var overrides = new HashMap<String, String>();
        overrides.put("prefix", warehouse);

        var config = new ConfigResponse(defaults, overrides);

        return Response.ok(config).build();
    }

    private Optional<Response> deny(SecurityContext securityContext, Permission permission) {
        return CatalogHandlerAuthorization.denyUnless(authorizationService, securityContext, DOMAIN, permission);
    }
}
