package kasanari.repository.paimon.yugabyte;

import kasanari.repository.paimon.model.ViewRecord;
import kasanari.repository.paimon.ViewRepository;
import org.apache.paimon.catalog.Identifier;
import org.jdbi.v3.core.Handle;

import java.util.List;
import java.util.Optional;

public class JdbcViewRepository implements ViewRepository<Handle> {
    private final String catalogName;

    public JdbcViewRepository(String catalogName) {
        this.catalogName = catalogName;
    }

    @Override
    public List<ViewRecord> findAll(Handle tx, String database) {
        var query = tx.createQuery(JdbcQueries.LIST_VIEWS);
        query.bind(0, catalogName);
        query.bind(1, database);

        return query
                .map((rs, ctx) -> new ViewRecord(
                        database,
                        rs.getString("view_name"),
                        rs.getString("query"),
                        JsonSerde.decodeMap(rs.getString("dialects_payload")),
                        JsonSerde.decodeMap(rs.getString("options_payload")),
                        Optional.ofNullable(rs.getString("comment"))
                ))
                .list();
    }

    @Override
    public List<ViewRecord> findPage(Handle tx, String database, String viewNamePatternLike, long idAfter, int pageSize) {
        var query = tx.createQuery(JdbcQueries.LIST_VIEWS_PAGE);
        query.bind(0, catalogName);
        query.bind(1, database);
        query.bind(2, idAfter);
        query.bind(3, viewNamePatternLike);
        query.bind(4, pageSize);
        return query
                .map((rs, ctx) -> new ViewRecord(
                        database,
                        rs.getString("view_name"),
                        rs.getString("query"),
                        JsonSerde.decodeMap(rs.getString("dialects_payload")),
                        JsonSerde.decodeMap(rs.getString("options_payload")),
                        Optional.ofNullable(rs.getString("comment")),
                        rs.getLong("id")
                ))
                .list();
    }

    @Override
    public List<ViewRecord> findPageGlobally(
            Handle tx,
            String databaseNamePatternLike,
            String viewNamePatternLike,
            long idAfter,
            int pageSize) {
        var query = tx.createQuery(JdbcQueries.LIST_VIEWS_PAGE_GLOBALLY);
        query.bind(0, catalogName);
        query.bind(1, idAfter);
        query.bind(2, databaseNamePatternLike);
        query.bind(3, viewNamePatternLike);
        query.bind(4, pageSize);
        return query
                .map((rs, ctx) -> new ViewRecord(
                        rs.getString("database_name"),
                        rs.getString("view_name"),
                        rs.getString("query"),
                        JsonSerde.decodeMap(rs.getString("dialects_payload")),
                        JsonSerde.decodeMap(rs.getString("options_payload")),
                        Optional.ofNullable(rs.getString("comment")),
                        rs.getLong("id")
                ))
                .list();
    }

    @Override
    public boolean delete(Handle tx, Identifier identifier) {
        var query = tx.createUpdate(JdbcQueries.DELETE_VIEW);
        query.bind(0, catalogName);
        query.bind(1, identifier.getDatabaseName());
        query.bind(2, identifier.getTableName());
        return query.execute() == 1;
    }

    @Override
    public void create(Handle tx, ViewRecord record) {
        var query = tx.createUpdate(JdbcQueries.INSERT_VIEW);
        query.bind(0, catalogName);
        query.bind(1, record.database());
        query.bind(2, record.name());
        query.bind(3, record.query());
        query.bind(4, JsonSerde.encodeMap(record.dialects()));
        query.bind(5, JsonSerde.encodeMap(record.options()));
        query.bind(6, record.comment().orElse(null));
        query.execute();
    }

    @Override
    public void alter(Handle tx, ViewRecord record) {
        var query = tx.createUpdate(JdbcQueries.UPDATE_VIEW);
        query.bind(0, record.query());
        query.bind(1, JsonSerde.encodeMap(record.dialects()));
        query.bind(2, JsonSerde.encodeMap(record.options()));
        query.bind(3, record.comment().orElse(null));
        query.bind(4, catalogName);
        query.bind(5, record.database());
        query.bind(6, record.name());
        query.execute();
    }

    @Override
    public boolean rename(Handle tx, Identifier from, Identifier to) {
        var query = tx.createUpdate(JdbcQueries.RENAME_VIEW);
        query.bind(0, to.getDatabaseName());
        query.bind(1, to.getTableName());
        query.bind(2, catalogName);
        query.bind(3, from.getDatabaseName());
        query.bind(4, from.getTableName());
        return query.execute() == 1;
    }

    @Override
    public Optional<ViewRecord> find(Handle tx, Identifier view) {
        var query = tx.createQuery(JdbcQueries.SELECT_VIEW);
        query.bind(0, catalogName);
        query.bind(1, view.getDatabaseName());
        query.bind(2, view.getTableName());

        return query
                .map((rs, ctx) -> new ViewRecord(
                        view.getDatabaseName(),
                        rs.getString("view_name"),
                        rs.getString("query"),
                        JsonSerde.decodeMap(rs.getString("dialects_payload")),
                        JsonSerde.decodeMap(rs.getString("options_payload")),
                        Optional.ofNullable(rs.getString("comment"))
                ))
                .findFirst();
    }
}
