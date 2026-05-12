package kasanari.repository.paimon.postgres;

import kasanari.repository.paimon.model.DatabaseRecord;
import kasanari.repository.paimon.DatabaseRepository;
import org.jdbi.v3.core.Handle;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class JdbcDatabaseRepository implements DatabaseRepository<Handle> {
    private final String catalogKey;

    public JdbcDatabaseRepository(String catalogKey) {
        this.catalogKey = catalogKey;
    }

    @Override
    public Optional<DatabaseRecord> findByName(Handle tx, String name) {
        var query = tx.createQuery(JdbcQueries.SELECT_DATABASE);
        query.bind(0, catalogKey);
        query.bind(1, name);

        return query
                .map((rs, ctx) -> new DatabaseRecord(
                        rs.getString("database_name"),
                        JsonSerde.decodeMap(rs.getString("options_payload")),
                        Optional.ofNullable(rs.getString("comment"))
                ))
                .findFirst();
    }

    @Override
    public void create(Handle tx, DatabaseRecord record) {
        var query = tx.createUpdate(JdbcQueries.INSERT_DATABASE);
        query.bind(0, catalogKey);
        query.bind(1, record.name());
        query.bind(2, JsonSerde.encodeMap(record.options()));
        query.bind(3, record.comment().orElse(null));
        query.execute();
    }

    @Override
    public boolean delete(Handle tx, String name) {
        var query = tx.createUpdate(JdbcQueries.DELETE_DATABASE);
        query.bind(0, catalogKey);
        query.bind(1, name);
        return query.execute() == 1;
    }

    @Override
    public boolean alter(Handle tx, String name, Map<String, String> update, Set<String> remove) {
        var maybe = findByName(tx, name);
        if (maybe.isEmpty()) {
            return false;
        }

        var current = maybe.get();
        var merged = new java.util.HashMap<>(current.options());
        merged.putAll(update);
        remove.forEach(merged::remove);

        var query = tx.createUpdate(JdbcQueries.UPDATE_DATABASE);
        query.bind(0, JsonSerde.encodeMap(merged));
        query.bind(1, catalogKey);
        query.bind(2, name);
        return query.execute() == 1;
    }

    @Override
    public List<DatabaseRecord> findAll(Handle tx) {
        var query = tx.createQuery(JdbcQueries.LIST_DATABASES);
        query.bind(0, catalogKey);
        return query
                .map((rs, ctx) -> new DatabaseRecord(
                        rs.getString("database_name"),
                        JsonSerde.decodeMap(rs.getString("options_payload")),
                        Optional.ofNullable(rs.getString("comment"))
                ))
                .list();
    }
}
