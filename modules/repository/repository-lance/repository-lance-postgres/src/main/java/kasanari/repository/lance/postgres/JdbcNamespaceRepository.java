package kasanari.repository.lance.postgres;

import kasanari.repository.lance.NamespaceRepository;
import org.jdbi.v3.core.Handle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcNamespaceRepository implements NamespaceRepository<Handle> {
    public JdbcNamespaceRepository() {
    }

    @Override
    public void upsert(Handle tx, String namespacePath, Map<String, String> properties) {
        tx.createUpdate("""
                        INSERT INTO kasanari_lance_namespaces(namespace_path, properties)
                        VALUES (:namespace_path, :properties::jsonb)
                        ON CONFLICT (namespace_path)
                        DO UPDATE SET properties = EXCLUDED.properties::jsonb
                        """)
                .bind("namespace_path", namespacePath)
                .bind("properties", JsonSerde.encodeMap(properties))
                .execute();
    }

    @Override
    public boolean exists(Handle tx, String namespacePath) {
        return tx.createQuery("""
                        SELECT 1 FROM kasanari_lance_namespaces
                        WHERE namespace_path = :namespace_path
                        LIMIT 1
                        """)
                .bind("namespace_path", namespacePath)
                .mapTo(Integer.class)
                .findOne()
                .isPresent();
    }

    @Override
    public Map<String, String> properties(Handle tx, String namespacePath) {
        return tx.createQuery("""
                        SELECT properties FROM kasanari_lance_namespaces
                        WHERE namespace_path = :namespace_path
                        LIMIT 1
                        """)
                .bind("namespace_path", namespacePath)
                .mapTo(String.class)
                .findOne()
                .map(JsonSerde::decodeMap)
                .orElse(new HashMap<>());
    }

    @Override
    public List<String> list(Handle tx, String parentPath) {
        var prefix = (parentPath == null || parentPath.isBlank()) ? "" : parentPath + ".";
        var rows = tx.createQuery("""
                        SELECT namespace_path FROM kasanari_lance_namespaces
                        ORDER BY namespace_path
                        """)
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
    public void delete(Handle tx, String namespacePath) {
        tx.createUpdate("""
                        DELETE FROM kasanari_lance_namespaces
                        WHERE namespace_path = :namespace_path
                        """)
                .bind("namespace_path", namespacePath)
                .execute();
    }
}
