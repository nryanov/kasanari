package kasanari.server.infrastructure.paimon;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestConfigService;
import kasanari.server.infrastructure.http.ApiFallbacks;

@ApplicationScoped
public class PaimonConfigServiceHandler implements PaimonRestConfigService {
    @Override
    public Response getConfig(String warehouse, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("PaimonConfigService.getConfig");
    }
}
