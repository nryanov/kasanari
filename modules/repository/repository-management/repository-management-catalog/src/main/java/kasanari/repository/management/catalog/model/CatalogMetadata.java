package kasanari.repository.management.catalog.model;

import kasanari.repository.management.common.model.CatalogType;

public record CatalogMetadata(
        String catalogName,
        CatalogType catalogType,
        CatalogMode catalogMode,
        CatalogSpec spec,
        long version
) {
}
