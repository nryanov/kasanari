package kasanari.server.infrastructure.paimon;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestConfigService;
import org.apache.paimon.rest.responses.ConfigResponse;

import java.util.Map;

@ApplicationScoped
public class PaimonConfigServiceHandler implements PaimonRestConfigService {
    @Override
    public Response getConfig(String warehouse, SecurityContext securityContext) {
        var config = new ConfigResponse(
                Map.of(),
                Map.of(
                        "prefix", warehouse
                )
        );

        return Response.ok(config).build();
    }
}
