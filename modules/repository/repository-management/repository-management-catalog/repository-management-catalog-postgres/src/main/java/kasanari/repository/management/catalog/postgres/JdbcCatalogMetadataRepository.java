package kasanari.repository.management.catalog.postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kasanari.repository.management.catalog.CatalogMetadataRepository;
import kasanari.repository.management.catalog.model.CatalogMode;
import kasanari.repository.management.catalog.model.CatalogMetadata;
import kasanari.repository.management.catalog.model.CatalogSpec;
import kasanari.repository.management.common.model.CatalogType;
import org.jdbi.v3.core.Handle;

import java.util.Optional;

public class JdbcCatalogMetadataRepository implements CatalogMetadataRepository<Handle> {
    private final ObjectMapper objectMapper;

    public JdbcCatalogMetadataRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<CatalogMetadata> getById(Handle tx, String catalogId) {
        var query = tx.createQuery(JdbcManagementCatalogQueries.SELECT_CATALOG);
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

        var insert = tx.createUpdate(JdbcManagementCatalogQueries.INSERT_CATALOG);
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
        var update = tx.createUpdate(JdbcManagementCatalogQueries.UPDATE_CATALOG);
        update.bind(0, serialize(spec));
        update.bind(1, nextVersion);
        update.bind(2, catalogId);
        update.bind(3, expectedVersion);
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
        var delete = tx.createUpdate(JdbcManagementCatalogQueries.DELETE_CATALOG);
        delete.bind(0, catalogId);
        return delete.execute() > 0;
    }

    private String serialize(CatalogSpec spec) {
        try {
            return objectMapper.writeValueAsString(spec);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize catalog spec JSON", e);
        }
    }
}
