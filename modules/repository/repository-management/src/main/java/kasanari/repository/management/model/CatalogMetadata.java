package kasanari.repository.management.model;

import kasanari.catalog.management.model.CatalogMode;
import kasanari.catalog.management.model.CatalogSpec;
import kasanari.catalog.management.model.CatalogType;

public record CatalogMetadata(
        String catalogId,
        CatalogType catalogType,
        CatalogMode catalogMode,
        CatalogSpec spec,
        long version
) {
}
