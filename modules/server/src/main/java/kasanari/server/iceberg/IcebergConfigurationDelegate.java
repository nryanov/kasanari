package kasanari.server.iceberg;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import kasanari.api.iceberg.IcebergConfigurationApi;
import kasanari.api.iceberg.dto.IcebergCatalogConfig;

@ApplicationScoped
public class IcebergConfigurationDelegate implements IcebergConfigurationApi {
    @Override
    public Uni<IcebergCatalogConfig> getConfig(String warehouse) {
        return null;
    }
}
