package kasanari.catalog.paimon.repository.jdbc;

import kasanari.catalog.paimon.model.TagRecord;
import kasanari.catalog.paimon.repository.TagRepository;
import org.apache.paimon.catalog.Identifier;
import org.jdbi.v3.core.Handle;

import java.util.List;
import java.util.Optional;

public class JdbcTagRepository implements TagRepository<Handle> {
    private final String catalogKey;

    public JdbcTagRepository(String catalogKey) {
        this.catalogKey = catalogKey;
    }

    @Override
    public void create(Handle tx, TagRecord record, boolean ignoreIfExists) {
        var query = tx.createUpdate(ignoreIfExists ? JdbcQueries.INSERT_TAG_IGNORE_IF_EXISTS : JdbcQueries.INSERT_TAG);
        query.bind(0, catalogKey);
        query.bind(1, record.database());
        query.bind(2, record.table());
        query.bind(3, record.tagName());
        query.bind(4, record.snapshotId());
        query.bind(5, record.tagCreateTime().orElse(null));
        query.bind(6, record.tagTimeRetained().orElse(null));
        query.execute();
    }

    @Override
    public boolean delete(Handle tx, Identifier identifier, String tagName) {
        var query = tx.createUpdate(JdbcQueries.DELETE_TAG);
        query.bind(0, catalogKey);
        query.bind(1, identifier.getDatabaseName());
        query.bind(2, identifier.getTableName());
        query.bind(3, tagName);
        return query.execute() == 1;
    }

    @Override
    public Optional<TagRecord> find(Handle tx, Identifier identifier, String tagName) {
        var query = tx.createQuery(JdbcQueries.SELECT_TAG);
        query.bind(0, catalogKey);
        query.bind(1, identifier.getDatabaseName());
        query.bind(2, identifier.getTableName());
        query.bind(3, tagName);
        return query
                .map((rs, ctx) -> new TagRecord(
                        identifier.getDatabaseName(),
                        identifier.getTableName(),
                        rs.getString("tag_name"),
                        rs.getLong("snapshot_id"),
                        Optional.ofNullable((Long) rs.getObject("tag_create_time")),
                        Optional.ofNullable(rs.getString("tag_time_retained")),
                        0L
                ))
                .findFirst();
    }

    @Override
    public boolean exists(Handle tx, Identifier identifier, String tagName) {
        var query = tx.createQuery(JdbcQueries.CHECK_TAG_EXISTS);
        query.bind(0, catalogKey);
        query.bind(1, identifier.getDatabaseName());
        query.bind(2, identifier.getTableName());
        query.bind(3, tagName);
        return query.mapTo(Boolean.class).first();
    }

    @Override
    public List<String> findAll(Handle tx, Identifier identifier, Optional<String> tagNamePrefix) {
        var query = tx.createQuery(tagNamePrefix.isPresent() ? JdbcQueries.LIST_TAGS_WITH_PREFIX : JdbcQueries.LIST_TAGS);
        query.bind(0, catalogKey);
        query.bind(1, identifier.getDatabaseName());
        query.bind(2, identifier.getTableName());
        tagNamePrefix.ifPresent(prefix -> query.bind(3, prefix + "%"));
        return query.mapTo(String.class).list();
    }

    @Override
    public List<TagRecord> findPage(
            Handle tx,
            Identifier identifier,
            String tagNamePatternLike,
            long idAfter,
            int pageSize) {
        var query = tx.createQuery(JdbcQueries.LIST_TAGS_PAGE);
        query.bind(0, catalogKey);
        query.bind(1, identifier.getDatabaseName());
        query.bind(2, identifier.getTableName());
        query.bind(3, idAfter);
        query.bind(4, tagNamePatternLike);
        query.bind(5, pageSize);
        return query
                .map((rs, ctx) -> new TagRecord(
                        identifier.getDatabaseName(),
                        identifier.getTableName(),
                        rs.getString("tag_name"),
                        rs.getLong("snapshot_id"),
                        Optional.ofNullable((Long) rs.getObject("tag_create_time")),
                        Optional.ofNullable(rs.getString("tag_time_retained")),
                        rs.getLong("id")
                ))
                .list();
    }
}
