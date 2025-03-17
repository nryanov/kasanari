package kasanari.server.iceberg;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import kasanari.api.iceberg.ConfigurationApiApi;
import kasanari.api.iceberg.dto.IcebergCatalogConfig;

@ApplicationScoped
public class IcebergConfigurationApiApi implements ConfigurationApiApi {
    @Override
    public Uni<IcebergCatalogConfig> getConfig(String warehouse) {
        return null;
    }
}
