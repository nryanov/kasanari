package kasanari.catalog.lance.jdbc;

import kasanari.repository.jdbc.KasanariDataSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableJdbcRepository {
    private final KasanariDataSource dataSource;

    public TableJdbcRepository(KasanariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void upsert(String tableId, String namespacePath, String tableName, String location, Map<String, String> properties, boolean declaredOnly) {
        dataSource.getJdbi().useHandle(handle -> handle.createUpdate("""
                        INSERT INTO kasanari_lance_tables(table_id, namespace_path, table_name, location, properties, declared_only)
                        VALUES (:table_id, :namespace_path, :table_name, :location, :properties, :declared_only)
                        ON CONFLICT (table_id)
                        DO UPDATE SET
                            namespace_path = EXCLUDED.namespace_path,
                            table_name = EXCLUDED.table_name,
                            location = EXCLUDED.location,
                            properties = EXCLUDED.properties,
                            declared_only = EXCLUDED.declared_only
                        """)
                .bind("table_id", tableId)
                .bind("namespace_path", namespacePath)
                .bind("table_name", tableName)
                .bind("location", location)
                .bind("properties", PropertiesSerde.encode(properties))
                .bind("declared_only", declaredOnly)
                .execute());
    }

    public boolean exists(String tableId) {
        return dataSource.getJdbi().withHandle(handle -> handle.createQuery("""
                        SELECT 1 FROM kasanari_lance_tables
                        WHERE table_id = :table_id
                        LIMIT 1
                        """)
                .bind("table_id", tableId)
                .mapTo(Integer.class)
                .findOne()
                .isPresent());
    }

    public TableRow get(String tableId) {
        return dataSource.getJdbi().withHandle(handle -> handle.createQuery("""
                        SELECT table_id, namespace_path, table_name, location, properties, declared_only
                        FROM kasanari_lance_tables
                        WHERE table_id = :table_id
                        LIMIT 1
                        """)
                .bind("table_id", tableId)
                .map((rs, ctx) -> new TableRow(
                        rs.getString("table_id"),
                        rs.getString("namespace_path"),
                        rs.getString("table_name"),
                        rs.getString("location"),
                        PropertiesSerde.decode(rs.getString("properties")),
                        rs.getBoolean("declared_only")
                ))
                .findOne()
                .orElse(null));
    }

    public List<String> listByNamespace(String namespacePath) {
        return dataSource.getJdbi().withHandle(handle -> handle.createQuery("""
                        SELECT table_id FROM kasanari_lance_tables
                        WHERE namespace_path = :namespace_path
                        ORDER BY table_name
                        """)
                .bind("namespace_path", namespacePath)
                .mapTo(String.class)
                .list());
    }

    public void delete(String tableId) {
        dataSource.getJdbi().useHandle(handle -> handle.createUpdate("""
                        DELETE FROM kasanari_lance_tables
                        WHERE table_id = :table_id
                        """)
                .bind("table_id", tableId)
                .execute());
    }

    public record TableRow(
            String tableId,
            String namespacePath,
            String tableName,
            String location,
            Map<String, String> properties,
            boolean declaredOnly
    ) {
        public Map<String, String> propertiesOrEmpty() {
            return properties == null ? new HashMap<>() : properties;
        }
    }
}
