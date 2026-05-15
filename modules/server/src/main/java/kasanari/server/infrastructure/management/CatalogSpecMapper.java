package kasanari.server.infrastructure.management;

import kasanari.catalog.management.dto.CatalogModeDto;
import kasanari.catalog.management.dto.CatalogSpecDto;
import kasanari.catalog.management.dto.CatalogTypeDto;
import kasanari.repository.management.catalog.model.CatalogMode;
import kasanari.repository.management.catalog.model.CatalogSpec;
import kasanari.repository.management.common.model.CatalogType;

public final class CatalogSpecMapper {
    private CatalogSpecMapper() {
    }

    public static CatalogSpec toDomain(CatalogSpecDto spec) {
        return new CatalogSpec(
                spec.getFileIoProperties(),
                spec.getCatalogProperties(),
                spec.getEndpoint()
        );
    }

    public static CatalogSpecDto toApi(CatalogSpec spec) {
        var copy = new CatalogSpecDto();
        copy.setFileIoProperties(spec.fileIoProperties());
        copy.setCatalogProperties(spec.catalogProperties());
        copy.setEndpoint(spec.endpoint());

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
}
