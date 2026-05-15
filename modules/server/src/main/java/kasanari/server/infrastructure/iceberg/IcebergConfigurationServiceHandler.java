package kasanari.server.infrastructure.iceberg;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.iceberg.IcebergCatalogConfigurationService;
import kasanari.catalog.iceberg.api.IcebergRestConfigurationApiService;

@ApplicationScoped
public class IcebergConfigurationServiceHandler implements IcebergRestConfigurationApiService {
    private final IcebergCatalogConfigurationService configurationService;

    public IcebergConfigurationServiceHandler(IcebergCatalogConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Override
    public Response getConfig(String warehouse, SecurityContext securityContext) {
        return Response.ok().entity(configurationService.getConfig(warehouse)).build();
    }
}
