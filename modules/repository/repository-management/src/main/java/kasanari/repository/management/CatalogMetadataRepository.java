package kasanari.repository.management;



import kasanari.catalog.management.model.CatalogSpec;
import kasanari.repository.management.model.CatalogMetadata;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CatalogMetadataRepository<T> {
    Optional<CatalogMetadata> getById(T tx, String catalogId);

    boolean create(T tx, CatalogMetadata metadata);

    Optional<CatalogMetadata> update(T tx, String catalogId, CatalogSpec spec, Long expectedVersion);

    boolean delete(T tx, String catalogId);

    void replaceSecrets(T tx, String catalogId, Map<String, String> secrets);

    List<String> getSecretKeys(T tx, String catalogId);
}
