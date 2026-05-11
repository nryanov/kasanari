package kasanari.server.paimon;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestConfigService;

@ApplicationScoped
public class PaimonConfigService implements PaimonRestConfigService {
    @Override
    public Response getConfig(String warehouse, SecurityContext securityContext) {
        return null;
    }
}
