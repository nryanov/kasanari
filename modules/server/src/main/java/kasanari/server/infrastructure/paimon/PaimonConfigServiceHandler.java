package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.catalog.paimon.api.PaimonRestConfigService;
import org.apache.paimon.rest.responses.ConfigResponse;

import java.util.HashMap;

@ApplicationScoped
public class PaimonConfigServiceHandler extends PaimonAuthorizedHandler implements PaimonRestConfigService {
    public PaimonConfigServiceHandler(AuthorizationService authorizationService) {
        super(authorizationService);
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
}
