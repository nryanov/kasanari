package kasanari.repository.management.catalog;


import kasanari.catalog.management.model.CatalogSpec;
import kasanari.repository.management.catalog.model.CatalogMetadata;

import java.util.Optional;

public interface CatalogMetadataRepository<T> {
    Optional<CatalogMetadata> getById(T tx, String catalogId);

    boolean create(T tx, CatalogMetadata metadata);

    Optional<CatalogMetadata> update(T tx, String catalogId, CatalogSpec spec, Long expectedVersion);

    boolean delete(T tx, String catalogId);
}
