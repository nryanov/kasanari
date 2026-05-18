package kasanari.server.infrastructure.paimon;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestConfigService;
import org.apache.paimon.rest.responses.ConfigResponse;

import java.util.HashMap;

@ApplicationScoped
public class PaimonConfigServiceHandler implements PaimonRestConfigService {
    @Override
    public Response getConfig(String warehouse, SecurityContext securityContext) {
        var defaults = new HashMap<String, String>();
        var overrides = new HashMap<String, String>();
        overrides.put("prefix", warehouse);

        var config = new ConfigResponse(defaults, overrides);

        return Response.ok(config).build();
    }
}
