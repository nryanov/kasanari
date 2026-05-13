package kasanari.catalog.lance.jdbc;

import kasanari.repository.jdbc.KasanariDataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NamespaceJdbcRepository {
    private final KasanariDataSource dataSource;

    public NamespaceJdbcRepository(KasanariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void upsert(String namespacePath, Map<String, String> properties) {
        dataSource.getJdbi().useHandle(handle -> handle.createUpdate("""
                        INSERT INTO kasanari_lance_namespaces(namespace_path, properties)
                        VALUES (:namespace_path, :properties)
                        ON CONFLICT (namespace_path)
                        DO UPDATE SET properties = EXCLUDED.properties
                        """)
                .bind("namespace_path", namespacePath)
                .bind("properties", PropertiesSerde.encode(properties))
                .execute());
    }

    public boolean exists(String namespacePath) {
        return dataSource.getJdbi().withHandle(handle -> handle.createQuery("""
                        SELECT 1 FROM kasanari_lance_namespaces
                        WHERE namespace_path = :namespace_path
                        LIMIT 1
                        """)
                .bind("namespace_path", namespacePath)
                .mapTo(Integer.class)
                .findOne()
                .isPresent());
    }

    public Map<String, String> properties(String namespacePath) {
        return dataSource.getJdbi().withHandle(handle -> handle.createQuery("""
                        SELECT properties FROM kasanari_lance_namespaces
                        WHERE namespace_path = :namespace_path
                        LIMIT 1
                        """)
                .bind("namespace_path", namespacePath)
                .mapTo(String.class)
                .findOne()
                .map(PropertiesSerde::decode)
                .orElse(new HashMap<>()));
    }

    public List<String> list(String parentPath) {
        var prefix = (parentPath == null || parentPath.isBlank()) ? "" : parentPath + ".";
        var rows = dataSource.getJdbi().withHandle(handle -> handle.createQuery("""
                        SELECT namespace_path FROM kasanari_lance_namespaces
                        ORDER BY namespace_path
                        """)
                .mapTo(String.class)
                .list());

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

    public void delete(String namespacePath) {
        dataSource.getJdbi().useHandle(handle -> handle.createUpdate("""
                        DELETE FROM kasanari_lance_namespaces
                        WHERE namespace_path = :namespace_path
                        """)
                .bind("namespace_path", namespacePath)
                .execute());
    }
}
