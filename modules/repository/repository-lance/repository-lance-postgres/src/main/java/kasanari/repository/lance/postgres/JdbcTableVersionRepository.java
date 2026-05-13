package kasanari.repository.lance.postgres;

import kasanari.repository.lance.TableVersionRepository;
import org.jdbi.v3.core.Handle;
import org.lance.namespace.model.TableVersion;
import org.lance.namespace.model.VersionRange;

import java.util.List;

public class JdbcTableVersionRepository implements TableVersionRepository<Handle> {
    public JdbcTableVersionRepository() {
    }

    @Override
    public void create(Handle tx, String tableId, TableVersion version) {
        tx.createUpdate("""
                        INSERT INTO kasanari_lance_table_versions(
                            table_id, version, manifest_path, manifest_size, etag, metadata, timestamp_millis
                        )
                        VALUES (
                            :table_id, :version, :manifest_path, :manifest_size, :etag, :metadata, :timestamp_millis
                        )
                        """)
                .bind("table_id", tableId)
                .bind("version", version.getVersion())
                .bind("manifest_path", version.getManifestPath())
                .bind("manifest_size", version.getManifestSize())
                .bind("etag", version.geteTag())
                .bind("metadata", PropertiesSerde.encode(version.getMetadata()))
                .bind("timestamp_millis", version.getTimestampMillis())
                .execute();
    }

    @Override
    public TableVersion get(Handle tx, String tableId, Long version) {
        return tx.createQuery("""
                        SELECT version, manifest_path, manifest_size, etag, metadata, timestamp_millis
                        FROM kasanari_lance_table_versions
                        WHERE table_id = :table_id AND version = :version
                        LIMIT 1
                        """)
                .bind("table_id", tableId)
                .bind("version", version)
                .map((rs, ctx) -> new TableVersion()
                        .version(rs.getLong("version"))
                        .manifestPath(rs.getString("manifest_path"))
                        .manifestSize(rs.getObject("manifest_size", Long.class))
                        .eTag(rs.getString("etag"))
                        .metadata(PropertiesSerde.decode(rs.getString("metadata")))
                        .timestampMillis(rs.getLong("timestamp_millis")))
                .findOne()
                .orElse(null);
    }

    @Override
    public List<TableVersion> list(Handle tx, String tableId, boolean descending, Integer limit, String pageToken) {
        var order = descending ? "DESC" : "ASC";
        var offset = pageToken == null || pageToken.isBlank() ? 0 : Integer.parseInt(pageToken);
        var max = limit == null ? 1000 : limit;

        return tx.createQuery("""
                        SELECT version, manifest_path, manifest_size, etag, metadata, timestamp_millis
                        FROM kasanari_lance_table_versions
                        WHERE table_id = :table_id
                        ORDER BY version %s
                        OFFSET :offset
                        LIMIT :limit
                        """.formatted(order))
                .bind("table_id", tableId)
                .bind("offset", offset)
                .bind("limit", max)
                .map((rs, ctx) -> new TableVersion()
                        .version(rs.getLong("version"))
                        .manifestPath(rs.getString("manifest_path"))
                        .manifestSize(rs.getObject("manifest_size", Long.class))
                        .eTag(rs.getString("etag"))
                        .metadata(PropertiesSerde.decode(rs.getString("metadata")))
                        .timestampMillis(rs.getLong("timestamp_millis")))
                .list();
    }

    @Override
    public long deleteRanges(Handle tx, String tableId, List<VersionRange> ranges) {
        final long[] count = {0L};

        for (var range : ranges) {
            var deleted = tx.createUpdate("""
                                DELETE FROM kasanari_lance_table_versions
                                WHERE table_id = :table_id
                                  AND version >= :start
                                  AND (:end = -1 OR version < :end)
                                """)
                    .bind("table_id", tableId)
                    .bind("start", range.getStartVersion())
                    .bind("end", range.getEndVersion())
                    .execute();
            count[0] += deleted;
        }

        return count[0];
    }

    @Override
    public void deleteForTable(Handle tx, String tableId) {
        tx.createUpdate("""
                        DELETE FROM kasanari_lance_table_versions
                        WHERE table_id = :table_id
                        """)
                .bind("table_id", tableId)
                .execute();
    }
}
