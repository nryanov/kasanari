package kasanari.repository.management.catalog.model;

import kasanari.core.model.CatalogType;

public record CatalogMetadata(
        String catalogName,
        CatalogType catalogType,
        CatalogMode catalogMode,
        CatalogSpec spec,
        long version
) {
}
