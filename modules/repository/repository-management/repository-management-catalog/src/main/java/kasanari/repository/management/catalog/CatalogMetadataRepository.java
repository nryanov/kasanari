package kasanari.repository.management.catalog;


import kasanari.repository.management.catalog.model.CatalogMetadata;
import kasanari.repository.management.catalog.model.CatalogSpec;
import kasanari.core.model.CatalogType;

import java.util.List;
import java.util.Optional;

public interface CatalogMetadataRepository<T> {
    List<CatalogMetadata> list(T tx, CatalogType catalogType);

    Optional<CatalogMetadata> getByName(T tx, CatalogType catalogType, String catalogName);

    boolean create(T tx, CatalogMetadata metadata);

    Optional<CatalogMetadata> update(T tx, CatalogType catalogType, String catalogName, CatalogSpec spec, Long expectedVersion);

    boolean delete(T tx, CatalogType catalogType, String catalogName);
}
