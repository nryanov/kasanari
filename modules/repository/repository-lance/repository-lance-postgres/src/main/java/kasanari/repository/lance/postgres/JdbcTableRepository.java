package kasanari.repository.lance.postgres;

import kasanari.repository.lance.TableRepository;
import kasanari.repository.lance.model.TableRow;
import org.jdbi.v3.core.Handle;

import java.util.List;
import java.util.Map;

public class JdbcTableRepository implements TableRepository<Handle> {
    public JdbcTableRepository() {
    }

    @Override
    public void upsert(Handle tx, String tableId, String namespacePath, String tableName, String location, Map<String, String> properties, boolean declaredOnly) {
        tx.createUpdate("""
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
                .execute();
    }

    @Override
    public boolean exists(Handle tx, String tableId) {
        return tx.createQuery("""
                        SELECT 1 FROM kasanari_lance_tables
                        WHERE table_id = :table_id
                        LIMIT 1
                        """)
                .bind("table_id", tableId)
                .mapTo(Integer.class)
                .findOne()
                .isPresent();
    }

    @Override
    public TableRow get(Handle tx, String tableId) {
        return tx.createQuery("""
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
                .orElse(null);
    }

    @Override
    public List<String> listByNamespace(Handle tx, String namespacePath) {
        return tx.createQuery("""
                        SELECT table_id FROM kasanari_lance_tables
                        WHERE namespace_path = :namespace_path
                        ORDER BY table_name
                        """)
                .bind("namespace_path", namespacePath)
                .mapTo(String.class)
                .list();
    }

    @Override
    public void delete(Handle tx, String tableId) {
        tx.createUpdate("""
                        DELETE FROM kasanari_lance_tables
                        WHERE table_id = :table_id
                        """)
                .bind("table_id", tableId)
                .execute();
    }
}
