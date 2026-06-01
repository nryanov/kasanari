package kasanari.repository.lance.postgres;

public final class JdbcQueries {
    private JdbcQueries() {
    }

    public static final String CREATE_NAMESPACES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_lance_namespaces (
                namespace_path TEXT PRIMARY KEY,
                properties JSONB NOT NULL DEFAULT '{}'::jsonb
            )
            """;

    public static final String CREATE_TABLES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_lance_tables (
                table_id TEXT PRIMARY KEY,
                namespace_path TEXT NOT NULL,
                table_name TEXT NOT NULL,
                location TEXT,
                properties JSONB NOT NULL DEFAULT '{}'::jsonb
            )
            """;

    public static final String UPSERT_NAMESPACE = """
            INSERT INTO kasanari_lance_namespaces(namespace_path, properties)
            VALUES (:namespace_path, :properties::jsonb)
            ON CONFLICT (namespace_path)
            DO UPDATE SET properties = EXCLUDED.properties::jsonb
            """;

    public static final String NAMESPACE_EXISTS = """
            SELECT 1 FROM kasanari_lance_namespaces
            WHERE namespace_path = :namespace_path
            LIMIT 1
            """;

    public static final String NAMESPACE_PROPERTIES = """
            SELECT properties FROM kasanari_lance_namespaces
            WHERE namespace_path = :namespace_path
            LIMIT 1
            """;

    public static final String LIST_NAMESPACES = """
            SELECT namespace_path FROM kasanari_lance_namespaces
            ORDER BY namespace_path
            """;

    public static final String DELETE_NAMESPACE = """
            DELETE FROM kasanari_lance_namespaces
            WHERE namespace_path = :namespace_path
            """;

    public static final String UPSERT_TABLE = """
            INSERT INTO kasanari_lance_tables(table_id, namespace_path, table_name, location, properties)
            VALUES (:table_id, :namespace_path, :table_name, :location, :properties::jsonb)
            ON CONFLICT (table_id)
            DO UPDATE SET
                namespace_path = EXCLUDED.namespace_path,
                table_name = EXCLUDED.table_name,
                location = EXCLUDED.location,
                properties = EXCLUDED.properties::jsonb
            """;

    public static final String TABLE_EXISTS = """
            SELECT 1 FROM kasanari_lance_tables
            WHERE table_id = :table_id
            LIMIT 1
            """;

    public static final String GET_TABLE = """
            SELECT table_id, namespace_path, table_name, location, properties
            FROM kasanari_lance_tables
            WHERE table_id = :table_id
            LIMIT 1
            """;

    public static final String LIST_TABLE_IDS_BY_NAMESPACE = """
            SELECT table_id FROM kasanari_lance_tables
            WHERE namespace_path = :namespace_path
            ORDER BY table_name
            """;

    public static final String DELETE_TABLE = """
            DELETE FROM kasanari_lance_tables
            WHERE table_id = :table_id
            """;
}
