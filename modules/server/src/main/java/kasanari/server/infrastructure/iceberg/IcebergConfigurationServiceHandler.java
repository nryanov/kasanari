package kasanari.server.infrastructure.iceberg;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.iceberg.api.IcebergRestConfigurationApiService;
import kasanari.server.infrastructure.http.ApiFallbacks;

@ApplicationScoped
public class IcebergConfigurationServiceHandler implements IcebergRestConfigurationApiService {
    @Override
    public Response getConfig(String warehouse, SecurityContext securityContext) {
        return ApiFallbacks.notImplemented("IcebergConfigurationService.getConfig");
    }
}
