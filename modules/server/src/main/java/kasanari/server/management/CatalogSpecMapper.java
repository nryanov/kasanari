package kasanari.server.management;

import kasanari.catalog.management.model.CatalogMode;
import kasanari.catalog.management.model.CatalogSpec;
import kasanari.catalog.management.model.CatalogType;
import kasanari.catalog.management.model.IcebergCatalogSpecModeConfig;

import java.util.HashMap;
import java.util.Map;

public final class CatalogSpecMapper {
    private CatalogSpecMapper() {
    }

    public static void validate(CatalogType expectedType, CatalogMode expectedMode, CatalogSpec spec) {
        if (spec == null || spec.getType() == null || spec.getModeConfig() == null) {
            throw new IllegalArgumentException("Catalog spec is required");
        }

        if (!expectedType.toString().equals(spec.getType().toString())) {
            throw new IllegalArgumentException("Catalog spec type must match catalogType");
        }

        var endpoint = spec.getModeConfig().getEndpoint();
        if (expectedMode == CatalogMode.PROXY && (endpoint == null || endpoint.isBlank())) {
            throw new IllegalArgumentException("Proxy mode requires modeConfig.endpoint");
        }
        if (expectedMode == CatalogMode.INTERNAL && endpoint != null && !endpoint.isBlank()) {
            throw new IllegalArgumentException("Internal mode must not contain modeConfig.endpoint");
        }
    }

    public static CatalogSpec copy(CatalogSpec spec) {
        var copy = new CatalogSpec();
        copy.setType(spec.getType());
        var mode = new IcebergCatalogSpecModeConfig();
        mode.setEndpoint(spec.getModeConfig().getEndpoint());
        mode.setProperties(spec.getModeConfig().getProperties() == null
                ? Map.of()
                : new HashMap<>(spec.getModeConfig().getProperties()));
        copy.setModeConfig(mode);
        return copy;
    }
}
