package kasanari.repository.lance.yugabyte;

import kasanari.repository.lance.TableRepository;
import kasanari.repository.lance.model.PagedValue;
import kasanari.repository.lance.model.TableMetadata;
import org.jdbi.v3.core.Handle;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JdbcTableRepository implements TableRepository<Handle> {
    private final String catalogKey;

    public JdbcTableRepository(String catalogKey) {
        this.catalogKey = catalogKey;
    }

    @Override
    public void upsert(Handle tx, String tableId, String namespacePath, String tableName, String location, Map<String, String> properties) {
        tx.createUpdate(JdbcQueries.UPSERT_TABLE)
                .bind("catalog_key", catalogKey)
                .bind("table_id", tableId)
                .bind("namespace_path", namespacePath)
                .bind("table_name", tableName)
                .bind("location", location)
                .bind("properties", JsonSerde.encodeMap(properties))
                .execute();
    }

    @Override
    public boolean exists(Handle tx, String tableId) {
        return tx.createQuery(JdbcQueries.TABLE_EXISTS)
                .bind("catalog_key", catalogKey)
                .bind("table_id", tableId)
                .mapTo(Integer.class)
                .findOne()
                .isPresent();
    }

    @Override
    public Optional<TableMetadata> get(Handle tx, String tableId) {
        return tx.createQuery(JdbcQueries.GET_TABLE)
                .bind("catalog_key", catalogKey)
                .bind("table_id", tableId)
                .map((rs, ctx) -> new TableMetadata(
                        rs.getString("table_id"),
                        rs.getString("namespace_path"),
                        rs.getString("table_name"),
                        rs.getString("location"),
                        JsonSerde.decodeMap(rs.getString("properties"))
                ))
                .findOne();
    }

    @Override
    public List<String> listByNamespace(Handle tx, String namespacePath) {
        return tx.createQuery(JdbcQueries.LIST_TABLE_IDS_BY_NAMESPACE)
                .bind("catalog_key", catalogKey)
                .bind("namespace_path", namespacePath)
                .mapTo(String.class)
                .list();
    }

    @Override
    public List<PagedValue<String>> listNamesByNamespacePage(Handle tx, String namespacePath, long cursorId, int limit) {
        return tx.createQuery(JdbcQueries.LIST_TABLE_NAMES_BY_NAMESPACE_PAGE)
                .bind("catalog_key", catalogKey)
                .bind("namespace_path", namespacePath)
                .bind("cursor_id", cursorId)
                .bind("limit", limit)
                .map((rs, ctx) -> new PagedValue<>(
                        rs.getLong("id"),
                        rs.getString("table_name")
                ))
                .list();
    }

    @Override
    public boolean delete(Handle tx, String tableId) {
        return tx.createUpdate(JdbcQueries.DELETE_TABLE)
                .bind("catalog_key", catalogKey)
                .bind("table_id", tableId)
                .execute() == 1;
    }
}
