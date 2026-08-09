package kasanari.repository.lance.yugabyte;

import kasanari.repository.lance.NamespaceRepository;
import kasanari.repository.lance.model.PagedValue;
import org.jdbi.v3.core.Handle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JdbcNamespaceRepository implements NamespaceRepository<Handle> {
    private final String catalogKey;

    public JdbcNamespaceRepository(String catalogKey) {
        this.catalogKey = catalogKey;
    }

    @Override
    public void upsert(Handle tx, String namespacePath, Map<String, String> properties) {
        tx.createUpdate(JdbcQueries.UPSERT_NAMESPACE)
                .bind("catalog_key", catalogKey)
                .bind("namespace_path", namespacePath)
                .bind("properties", JsonSerde.encodeMap(properties))
                .execute();
    }

    @Override
    public boolean exists(Handle tx, String namespacePath) {
        return tx.createQuery(JdbcQueries.NAMESPACE_EXISTS)
                .bind("catalog_key", catalogKey)
                .bind("namespace_path", namespacePath)
                .mapTo(Integer.class)
                .findOne()
                .isPresent();
    }

    @Override
    public Optional<Map<String, String>> properties(Handle tx, String namespacePath) {
        return tx.createQuery(JdbcQueries.NAMESPACE_PROPERTIES)
                .bind("catalog_key", catalogKey)
                .bind("namespace_path", namespacePath)
                .mapTo(String.class)
                .findOne()
                .map(JsonSerde::decodeMap);
    }

    @Override
    public List<String> list(Handle tx, String parentPath) {
        var prefix = (parentPath == null || parentPath.isBlank()) ? "" : parentPath + ".";
        var rows = tx.createQuery(JdbcQueries.LIST_NAMESPACES)
                .bind("catalog_key", catalogKey)
                .mapTo(String.class)
                .list();

        var result = new ArrayList<String>();
        for (var row : rows) {
            if (!row.startsWith(prefix)) {
                continue;
            }

            var tail = row.substring(prefix.length());
            if (tail.isBlank()) {
                continue;
            }

            var child = tail.split("[.]")[0];
            if (!result.contains(child)) {
                result.add(child);
            }
        }
        return result;
    }

    @Override
    public List<PagedValue<String>> listPage(Handle tx, String parentPath, long cursorId, int limit) {
        return tx.createQuery(JdbcQueries.LIST_NAMESPACES_PAGE)
                .bind("catalog_key", catalogKey)
                .bind("cursor_id", cursorId)
                .bind("limit", limit)
                .map((rs, ctx) -> new PagedValue<>(
                        rs.getLong("id"),
                        rs.getString("namespace_path")
                ))
                .list();
    }

    @Override
    public void delete(Handle tx, String namespacePath) {
        tx.createUpdate(JdbcQueries.DELETE_NAMESPACE)
                .bind("catalog_key", catalogKey)
                .bind("namespace_path", namespacePath)
                .execute();
    }
}
