package kasanari.catalog.paimon.repository.jdbc;

import kasanari.catalog.paimon.model.TableRecord;
import kasanari.catalog.paimon.repository.TableRepository;
import org.apache.paimon.catalog.Identifier;
import org.jdbi.v3.core.Handle;

import java.util.List;
import java.util.Optional;

public class JdbcTableRepository implements TableRepository<Handle> {
    private final String catalogKey;

    public JdbcTableRepository(String catalogKey) {
        this.catalogKey = catalogKey;
    }

    @Override
    public List<TableRecord> findAll(Handle tx, String database) {
        var query = tx.createQuery(JdbcQueries.LIST_TABLES);
        query.bind(0, catalogKey);
        query.bind(1, database);
        return query
                .map((rs, ctx) -> new TableRecord(
                        database,
                        rs.getString("table_name"),
                        JsonSerde.decodeMap(rs.getString("properties_payload")),
                        Optional.ofNullable(rs.getString("table_uuid"))
                ))
                .list();
    }

    @Override
    public boolean delete(Handle tx, Identifier identifier) {
        var query = tx.createUpdate(JdbcQueries.DELETE_TABLE);
        query.bind(0, catalogKey);
        query.bind(1, identifier.getDatabaseName());
        query.bind(2, identifier.getTableName());
        return query.execute() == 1;
    }

    @Override
    public void create(Handle tx, TableRecord record) {
        var query = tx.createUpdate(JdbcQueries.INSERT_TABLE);
        query.bind(0, catalogKey);
        query.bind(1, record.database());
        query.bind(2, record.name());
        query.bind(3, record.tableUuid().orElse(null));
        query.bind(4, JsonSerde.encodeMap(record.properties()));
        query.execute();
    }

    @Override
    public void alter(Handle tx, TableRecord record) {
        var query = tx.createUpdate(JdbcQueries.UPDATE_TABLE);
        query.bind(0, JsonSerde.encodeMap(record.properties()));
        query.bind(1, catalogKey);
        query.bind(2, record.database());
        query.bind(3, record.name());
        query.execute();
    }

    @Override
    public void rename(Handle tx, Identifier fromTable, Identifier toTable) {
        var query = tx.createUpdate(JdbcQueries.RENAME_TABLE);
        query.bind(0, toTable.getDatabaseName());
        query.bind(1, toTable.getTableName());
        query.bind(2, catalogKey);
        query.bind(3, fromTable.getDatabaseName());
        query.bind(4, fromTable.getTableName());
        query.execute();
    }

    @Override
    public boolean exists(Handle tx, Identifier table) {
        var query = tx.createQuery(JdbcQueries.CHECK_TABLE_EXISTS);
        query.bind(0, catalogKey);
        query.bind(1, table.getDatabaseName());
        query.bind(2, table.getTableName());
        return query.mapTo(Boolean.class).first();
    }

    @Override
    public Optional<TableRecord> find(Handle tx, Identifier table) {
        var query = tx.createQuery(JdbcQueries.SELECT_TABLE);
        query.bind(0, catalogKey);
        query.bind(1, table.getDatabaseName());
        query.bind(2, table.getTableName());
        return query
                .map((rs, ctx) -> new TableRecord(
                        table.getDatabaseName(),
                        rs.getString("table_name"),
                        JsonSerde.decodeMap(rs.getString("properties_payload")),
                        Optional.ofNullable(rs.getString("table_uuid"))
                ))
                .findFirst();
    }

    @Override
    public Optional<TableRecord> findByUuid(Handle tx, String tableUuid) {
        var query = tx.createQuery(JdbcQueries.SELECT_TABLE_BY_UUID);
        query.bind(0, catalogKey);
        query.bind(1, tableUuid);
        return query
                .map((rs, ctx) -> new TableRecord(
                        rs.getString("database_name"),
                        rs.getString("table_name"),
                        JsonSerde.decodeMap(rs.getString("properties_payload")),
                        Optional.ofNullable(rs.getString("table_uuid"))
                ))
                .findFirst();
    }
}
