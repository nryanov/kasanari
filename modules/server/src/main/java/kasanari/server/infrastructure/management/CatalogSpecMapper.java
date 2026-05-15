package kasanari.server.infrastructure.management;

import kasanari.catalog.management.dto.CatalogModeDto;
import kasanari.catalog.management.dto.CatalogSpecDto;
import kasanari.catalog.management.dto.CatalogTypeDto;
import kasanari.catalog.management.dto.IcebergCatalogSpecModeConfigDto;
import kasanari.repository.management.catalog.model.CatalogMode;
import kasanari.repository.management.catalog.model.CatalogSpecModeConfig;
import kasanari.repository.management.common.model.CatalogType;

import java.util.HashMap;
import java.util.Map;

public final class CatalogSpecMapper {
    private CatalogSpecMapper() {
    }

    public static void validate(CatalogType expectedType, CatalogMode expectedMode, CatalogSpecDto spec) {
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

    public static kasanari.repository.management.catalog.model.CatalogSpec toDomain(CatalogSpecDto spec) {
        var copy = new kasanari.repository.management.catalog.model.CatalogSpec();
        copy.setType(mapTypeToDomain(spec.getType()));
        var mode = new CatalogSpecModeConfig();
        mode.setEndpoint(spec.getModeConfig() == null ? null : spec.getModeConfig().getEndpoint());
        mode.setProperties(spec.getModeConfig() == null || spec.getModeConfig().getProperties() == null
                ? Map.of()
                : new HashMap<>(spec.getModeConfig().getProperties()));
        copy.setModeConfig(mode);
        return copy;
    }

    public static CatalogSpecDto toApi(kasanari.repository.management.catalog.model.CatalogSpec spec) {
        var copy = new CatalogSpecDto();
        copy.setType(mapTypeToApi(spec.getType()));
        var mode = new IcebergCatalogSpecModeConfigDto();
        mode.setEndpoint(spec.getModeConfig() == null ? null : spec.getModeConfig().getEndpoint());
        mode.setProperties(spec.getModeConfig() == null || spec.getModeConfig().getProperties() == null
                ? Map.of()
                : new HashMap<>(spec.getModeConfig().getProperties()));
        copy.setModeConfig(mode);
        return copy;
    }

    public static CatalogType toDomain(CatalogTypeDto type) {
        return switch (type) {
            case ICEBERG -> CatalogType.ICEBERG;
            case PAIMON -> CatalogType.PAIMON;
            case LANCE -> CatalogType.LANCE;
        };
    }

    public static CatalogTypeDto toApi(CatalogType type) {
        return switch (type) {
            case ICEBERG -> CatalogTypeDto.ICEBERG;
            case PAIMON -> CatalogTypeDto.PAIMON;
            case LANCE -> CatalogTypeDto.LANCE;
        };
    }

    public static CatalogMode toDomain(CatalogModeDto mode) {
        return switch (mode) {
            case INTERNAL -> CatalogMode.INTERNAL;
            case PROXY -> CatalogMode.PROXY;
        };
    }

    public static CatalogModeDto toApi(CatalogMode mode) {
        return switch (mode) {
            case INTERNAL -> CatalogModeDto.INTERNAL;
            case PROXY -> CatalogModeDto.PROXY;
        };
    }

    private static kasanari.repository.management.catalog.model.CatalogSpec.Type mapTypeToDomain(CatalogTypeDto type) {
        return switch (type) {
            case ICEBERG -> kasanari.repository.management.catalog.model.CatalogSpec.Type.ICEBERG;
            case PAIMON -> kasanari.repository.management.catalog.model.CatalogSpec.Type.PAIMON;
            case LANCE -> kasanari.repository.management.catalog.model.CatalogSpec.Type.LANCE;
        };
    }

    private static CatalogTypeDto mapTypeToApi(kasanari.repository.management.catalog.model.CatalogSpec.Type type) {
        return switch (type) {
            case ICEBERG -> CatalogTypeDto.ICEBERG;
            case PAIMON -> CatalogTypeDto.PAIMON;
            case LANCE -> CatalogTypeDto.LANCE;
        };
    }
}
