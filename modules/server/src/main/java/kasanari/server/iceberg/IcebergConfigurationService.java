package kasanari.server.iceberg;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.iceberg.api.IcebergRestConfigurationApiService;

@ApplicationScoped
public class IcebergConfigurationService implements IcebergRestConfigurationApiService {
    @Override
    public Response getConfig(String warehouse, SecurityContext securityContext) {
        return IcebergRestConfigurationApiService.super.getConfig(warehouse, securityContext);
    }
}
