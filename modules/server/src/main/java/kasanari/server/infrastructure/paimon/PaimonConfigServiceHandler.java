package kasanari.server.infrastructure.paimon;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.paimon.api.PaimonRestConfigService;
import kasanari.authorization.spi.Permission;
import kasanari.instrumentation.spi.paimon.PaimonCatalogOperation;
import kasanari.server.infrastructure.instrumentation.PaimonCatalogRequestExecutor;
import org.apache.paimon.rest.responses.ConfigResponse;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class PaimonConfigServiceHandler implements PaimonRestConfigService {
    private final PaimonCatalogRequestExecutor executor;

    public PaimonConfigServiceHandler(PaimonCatalogRequestExecutor executor) {
        this.executor = executor;
    }

    @Override
    public Response getConfig(String warehouse, SecurityContext securityContext) {
        return executor.execute(securityContext, warehouse, PaimonCatalogOperation.GET_CONFIG, Permission.PaimonConfigGet, Map.of("warehouse", warehouse),
                () -> {
                    var defaults = new HashMap<String, String>();
                    var overrides = new HashMap<String, String>();
                    overrides.put("prefix", warehouse);

                    var config = new ConfigResponse(defaults, overrides);

                    return Response.ok(config).build();
                }
        );
    }
}
