package kasanari.repository.management.postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kasanari.catalog.management.model.CatalogMode;
import kasanari.catalog.management.model.CatalogSpec;
import kasanari.catalog.management.model.CatalogType;
import kasanari.repository.management.CatalogMetadataRepository;
import kasanari.repository.management.model.CatalogMetadata;
import org.jdbi.v3.core.Handle;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JdbcCatalogMetadataRepository implements CatalogMetadataRepository<Handle> {
    private final ObjectMapper objectMapper;

    public JdbcCatalogMetadataRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<CatalogMetadata> getById(Handle tx, String catalogId) {
        var query = tx.createQuery(JdbcManagementQueries.SELECT_CATALOG);
        query.bind(0, catalogId);

        return query.map((rs, ctx) -> {
            try {
                var spec = objectMapper.readValue(rs.getString("spec_json"), CatalogSpec.class);
                return new CatalogMetadata(
                        rs.getString("catalog_id"),
                        CatalogType.fromValue(rs.getString("catalog_type")),
                        CatalogMode.fromValue(rs.getString("catalog_mode")),
                        spec,
                        rs.getLong("version")
                );
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to deserialize catalog spec JSON", e);
            }
        }).findFirst();
    }

    @Override
    public boolean create(Handle tx, CatalogMetadata metadata) {
        if (getById(tx, metadata.catalogId()).isPresent()) {
            return false;
        }

        var insert = tx.createUpdate(JdbcManagementQueries.INSERT_CATALOG);
        insert.bind(0, metadata.catalogId());
        insert.bind(1, metadata.catalogType().toString());
        insert.bind(2, metadata.catalogMode().toString());
        insert.bind(3, serialize(metadata.spec()));
        insert.bind(4, metadata.version());
        insert.execute();
        return true;
    }

    @Override
    public Optional<CatalogMetadata> update(Handle tx, String catalogId, CatalogSpec spec, Long expectedVersion) {
        var current = getById(tx, catalogId);

        if (current.isEmpty()) {
            return Optional.empty();
        }

        var existing = current.get();
        if (expectedVersion != null && expectedVersion != existing.version()) {
            throw new IllegalStateException("Catalog version does not match expected value");
        }

        var nextVersion = existing.version() + 1;
        var update = tx.createUpdate(JdbcManagementQueries.UPDATE_CATALOG);
        update.bind(0, serialize(spec));
        update.bind(1, nextVersion);
        update.bind(2, catalogId);
        update.execute();

        return Optional.of(new CatalogMetadata(
                existing.catalogId(),
                existing.catalogType(),
                existing.catalogMode(),
                spec,
                nextVersion
        ));
    }

    @Override
    public boolean delete(Handle tx, String catalogId) {
        var delete = tx.createUpdate(JdbcManagementQueries.DELETE_CATALOG);
        delete.bind(0, catalogId);
        return delete.execute() > 0;
    }

    @Override
    public void replaceSecrets(Handle tx, String catalogId, Map<String, String> secrets) {
        var delete = tx.createUpdate(JdbcManagementQueries.DELETE_SECRETS_BY_CATALOG);
        delete.bind(0, catalogId);
        delete.execute();

        if (secrets == null || secrets.isEmpty()) {
            return;
        }

        for (var entry : secrets.entrySet()) {
            var upsert = tx.createUpdate(JdbcManagementQueries.UPSERT_SECRET);
            upsert.bind(0, catalogId);
            upsert.bind(1, entry.getKey());
            upsert.bind(2, entry.getValue());
            upsert.execute();
        }
    }

    @Override
    public List<String> getSecretKeys(Handle tx, String catalogId) {
        var query = tx.createQuery(JdbcManagementQueries.SELECT_SECRET_KEYS_BY_CATALOG);
        query.bind(0, catalogId);
        return query.mapTo(String.class).list();
    }

    private String serialize(CatalogSpec spec) {
        try {
            return objectMapper.writeValueAsString(spec);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize catalog spec JSON", e);
        }
    }
}
