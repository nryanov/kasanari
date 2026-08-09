package kasanari.repository.management.catalog.yugabyte;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kasanari.repository.management.catalog.CatalogMetadataRepository;
import kasanari.repository.management.catalog.model.CatalogMode;
import kasanari.repository.management.catalog.model.CatalogMetadata;
import kasanari.repository.management.catalog.model.CatalogSpec;
import kasanari.core.model.CatalogType;
import org.jdbi.v3.core.Handle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcCatalogMetadataRepository implements CatalogMetadataRepository<Handle> {
    private final ObjectMapper objectMapper;

    public JdbcCatalogMetadataRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<CatalogMetadata> list(Handle tx, CatalogType catalogType) {
        var query = tx.createQuery(JdbcManagementCatalogQueries.SELECT_CATALOGS_BY_TYPE);
        query.bind(0, catalogType.toString());

        return query.map((rs, ctx) -> mapCatalogMetadata(rs)).list();
    }

    @Override
    public Optional<CatalogMetadata> getByName(Handle tx, CatalogType catalogType, String catalogName) {
        var query = tx.createQuery(JdbcManagementCatalogQueries.SELECT_CATALOG);
        query.bind(0, catalogType.toString());
        query.bind(1, catalogName);

        return query.map((rs, ctx) -> mapCatalogMetadata(rs)).findFirst();
    }

    @Override
    public boolean create(Handle tx, CatalogMetadata metadata) {
        if (getByName(tx, metadata.catalogType(), metadata.catalogName()).isPresent()) {
            return false;
        }

        var insert = tx.createUpdate(JdbcManagementCatalogQueries.INSERT_CATALOG);
        insert.bind(0, metadata.catalogType().toString());
        insert.bind(1, metadata.catalogName());
        insert.bind(2, metadata.catalogMode().toString());
        insert.bind(3, serialize(metadata.spec()));
        insert.bind(4, metadata.version());
        insert.execute();
        return true;
    }

    @Override
    public Optional<CatalogMetadata> update(Handle tx, CatalogType catalogType, String catalogName, CatalogSpec spec, Long expectedVersion) {
        var current = getByName(tx, catalogType, catalogName);

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
        update.bind(2, catalogType.toString());
        update.bind(3, catalogName);
        update.bind(4, existing.version());
        var rows = update.execute();
        if (rows == 0) {
            throw new IllegalStateException("Catalog version does not match expected value");
        }

        return Optional.of(new CatalogMetadata(
                existing.catalogName(),
                existing.catalogType(),
                existing.catalogMode(),
                spec,
                nextVersion
        ));
    }

    @Override
    public boolean delete(Handle tx, CatalogType catalogType, String catalogName) {
        var delete = tx.createUpdate(JdbcManagementCatalogQueries.DELETE_CATALOG);
        delete.bind(0, catalogType.toString());
        delete.bind(1, catalogName);
        return delete.execute() > 0;
    }

    private CatalogMetadata mapCatalogMetadata(ResultSet rs) throws SQLException {
        try {
            var spec = objectMapper.readValue(rs.getString("spec_json"), CatalogSpec.class);
            return new CatalogMetadata(
                    rs.getString("catalog_name"),
                    CatalogType.fromValue(rs.getString("catalog_type")),
                    CatalogMode.fromValue(rs.getString("catalog_mode")),
                    spec,
                    rs.getLong("version")
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize catalog spec JSON", e);
        }
    }

    private String serialize(CatalogSpec spec) {
        try {
            return objectMapper.writeValueAsString(spec);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize catalog spec JSON", e);
        }
    }
}
