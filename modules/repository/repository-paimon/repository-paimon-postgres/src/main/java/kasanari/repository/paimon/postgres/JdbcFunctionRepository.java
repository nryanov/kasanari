package kasanari.repository.paimon.postgres;

import kasanari.repository.paimon.model.FunctionRecord;
import kasanari.repository.paimon.FunctionRepository;
import org.apache.paimon.catalog.Identifier;
import org.jdbi.v3.core.Handle;

import java.util.List;
import java.util.Optional;

public class JdbcFunctionRepository implements FunctionRepository<Handle> {
    private final String catalogName;

    public JdbcFunctionRepository(String catalogName) {
        this.catalogName = catalogName;
    }

    @Override
    public List<FunctionRecord> findAll(Handle tx, String database) {
        var query = tx.createQuery(JdbcQueries.LIST_FUNCTIONS);
        query.bind(0, catalogName);
        query.bind(1, database);

        return query
                .map((rs, ctx) -> new FunctionRecord(
                        database,
                        rs.getString("function_name"),
                        rs.getBoolean("deterministic"),
                        JsonSerde.decodeDefinitions(rs.getString("definitions_payload")),
                        Optional.ofNullable(rs.getString("comment")),
                        JsonSerde.decodeMap(rs.getString("options_payload"))
                ))
                .list();
    }

    @Override
    public List<FunctionRecord> findPage(
            Handle tx,
            String database,
            String functionNamePatternLike,
            long idAfter,
            int pageSize) {
        var query = tx.createQuery(JdbcQueries.LIST_FUNCTIONS_PAGE);
        query.bind(0, catalogName);
        query.bind(1, database);
        query.bind(2, idAfter);
        query.bind(3, functionNamePatternLike);
        query.bind(4, pageSize);
        return query
                .map((rs, ctx) -> new FunctionRecord(
                        database,
                        rs.getString("function_name"),
                        rs.getBoolean("deterministic"),
                        JsonSerde.decodeDefinitions(rs.getString("definitions_payload")),
                        Optional.ofNullable(rs.getString("comment")),
                        JsonSerde.decodeMap(rs.getString("options_payload")),
                        rs.getLong("id")
                ))
                .list();
    }

    @Override
    public List<FunctionRecord> findPageGlobally(
            Handle tx,
            String databaseNamePatternLike,
            String functionNamePatternLike,
            long idAfter,
            int pageSize) {
        var query = tx.createQuery(JdbcQueries.LIST_FUNCTIONS_PAGE_GLOBALLY);
        query.bind(0, catalogName);
        query.bind(1, idAfter);
        query.bind(2, databaseNamePatternLike);
        query.bind(3, functionNamePatternLike);
        query.bind(4, pageSize);
        return query
                .map((rs, ctx) -> new FunctionRecord(
                        rs.getString("database_name"),
                        rs.getString("function_name"),
                        rs.getBoolean("deterministic"),
                        JsonSerde.decodeDefinitions(rs.getString("definitions_payload")),
                        Optional.ofNullable(rs.getString("comment")),
                        JsonSerde.decodeMap(rs.getString("options_payload")),
                        rs.getLong("id")
                ))
                .list();
    }

    @Override
    public Optional<FunctionRecord> find(Handle tx, Identifier function) {
        var query = tx.createQuery(JdbcQueries.SELECT_FUNCTION);
        query.bind(0, catalogName);
        query.bind(1, function.getDatabaseName());
        query.bind(2, function.getTableName());

        return query
                .map((rs, ctx) -> new FunctionRecord(
                        function.getDatabaseName(),
                        rs.getString("function_name"),
                        rs.getBoolean("deterministic"),
                        JsonSerde.decodeDefinitions(rs.getString("definitions_payload")),
                        Optional.ofNullable(rs.getString("comment")),
                        JsonSerde.decodeMap(rs.getString("options_payload"))
                ))
                .findFirst();
    }

    @Override
    public boolean delete(Handle tx, Identifier identifier) {
        var query = tx.createUpdate(JdbcQueries.DELETE_FUNCTION);
        query.bind(0, catalogName);
        query.bind(1, identifier.getDatabaseName());
        query.bind(2, identifier.getTableName());
        return query.execute() == 1;
    }

    @Override
    public void create(Handle tx, FunctionRecord record) {
        var query = tx.createUpdate(JdbcQueries.INSERT_FUNCTION);
        query.bind(0, catalogName);
        query.bind(1, record.database());
        query.bind(2, record.name());
        query.bind(3, record.deterministic());
        query.bind(4, JsonSerde.encodeDefinitions(record.definitions()));
        query.bind(5, JsonSerde.encodeMap(record.options()));
        query.bind(6, record.comment().orElse(null));
        query.execute();
    }

    @Override
    public void alter(Handle tx, FunctionRecord record) {
        var query = tx.createUpdate(JdbcQueries.UPDATE_FUNCTION);
        query.bind(0, record.deterministic());
        query.bind(1, JsonSerde.encodeDefinitions(record.definitions()));
        query.bind(2, JsonSerde.encodeMap(record.options()));
        query.bind(3, record.comment().orElse(null));
        query.bind(4, catalogName);
        query.bind(5, record.database());
        query.bind(6, record.name());
        query.execute();
    }
}
