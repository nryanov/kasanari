package kasanari.repository.management.catalog.model;

import kasanari.repository.management.common.model.CatalogType;

public record CatalogMetadata(
        String catalogId,
        CatalogType catalogType,
        CatalogMode catalogMode,
        CatalogSpec spec,
        long version
) {
}
