package kasanari.repository.lance.yugabyte;

public final class JdbcQueries {
    private JdbcQueries() {
    }

    public static final String CREATE_NAMESPACES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_lance_namespaces (
                id BIGINT GENERATED ALWAYS AS IDENTITY,
                catalog_name TEXT NOT NULL,
                namespace_path TEXT NOT NULL,
                properties JSONB NOT NULL DEFAULT '{}'::jsonb,
                CONSTRAINT kasanari_lance_namespaces_id_uq UNIQUE (id),
                CONSTRAINT kasanari_lance_namespaces_pk PRIMARY KEY (catalog_name HASH, namespace_path ASC)
            ) WITH (colocation = false)
            """;

    public static final String CREATE_TABLES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_lance_tables (
                id BIGINT GENERATED ALWAYS AS IDENTITY,
                catalog_name TEXT NOT NULL,
                table_id TEXT NOT NULL,
                namespace_path TEXT NOT NULL,
                table_name TEXT NOT NULL,
                location TEXT NOT NULL,
                properties JSONB NOT NULL DEFAULT '{}'::jsonb,
                CONSTRAINT kasanari_lance_tables_id_uq UNIQUE (id),
                CONSTRAINT kasanari_lance_tables_pk PRIMARY KEY (catalog_name HASH, table_id ASC),
                CONSTRAINT kasanari_lance_namespaces_fk FOREIGN KEY (catalog_name, namespace_path)
                    REFERENCES kasanari_lance_namespaces(catalog_name, namespace_path)
            ) WITH (colocation = false)
            """;

    public static final String CREATE_TABLES_FK_INDEX = """
            CREATE INDEX IF NOT EXISTS kasanari_lance_tables_fk_idx
            ON kasanari_lance_tables(catalog_name, namespace_path);
            """;

    public static final String UPSERT_NAMESPACE = """
            INSERT INTO kasanari_lance_namespaces(catalog_name, namespace_path, properties)
            VALUES (:catalog_name, :namespace_path, :properties::jsonb)
            ON CONFLICT (catalog_name, namespace_path)
            DO UPDATE SET properties = EXCLUDED.properties::jsonb
            """;

    public static final String NAMESPACE_EXISTS = """
            SELECT 1 FROM kasanari_lance_namespaces
            WHERE catalog_name = :catalog_name AND namespace_path = :namespace_path
            LIMIT 1
            """;

    public static final String NAMESPACE_PROPERTIES = """
            SELECT properties FROM kasanari_lance_namespaces
            WHERE catalog_name = :catalog_name AND namespace_path = :namespace_path
            LIMIT 1
            """;

    public static final String LIST_NAMESPACES = """
            SELECT namespace_path FROM kasanari_lance_namespaces
            WHERE catalog_name = :catalog_name
            ORDER BY namespace_path
            """;

    public static final String LIST_NAMESPACES_PAGE = """
            SELECT id, namespace_path
            FROM kasanari_lance_namespaces
            WHERE catalog_name = :catalog_name
              AND id > :cursor_id
            ORDER BY id
            LIMIT :limit
            """;

    public static final String DELETE_NAMESPACE = """
            DELETE FROM kasanari_lance_namespaces
            WHERE catalog_name = :catalog_name AND namespace_path = :namespace_path
            """;

    public static final String UPSERT_TABLE = """
            INSERT INTO kasanari_lance_tables(catalog_name, table_id, namespace_path, table_name, location, properties)
            VALUES (:catalog_name, :table_id, :namespace_path, :table_name, :location, :properties::jsonb)
            ON CONFLICT (catalog_name, table_id)
            DO UPDATE SET
                namespace_path = EXCLUDED.namespace_path,
                table_name = EXCLUDED.table_name,
                location = EXCLUDED.location,
                properties = EXCLUDED.properties::jsonb
            """;

    public static final String TABLE_EXISTS = """
            SELECT 1 FROM kasanari_lance_tables
            WHERE catalog_name = :catalog_name AND table_id = :table_id
            LIMIT 1
            """;

    public static final String GET_TABLE = """
            SELECT table_id, namespace_path, table_name, location, properties
            FROM kasanari_lance_tables
            WHERE catalog_name = :catalog_name AND table_id = :table_id
            LIMIT 1
            """;

    public static final String LIST_TABLE_IDS_BY_NAMESPACE = """
            SELECT table_id FROM kasanari_lance_tables
            WHERE catalog_name = :catalog_name AND namespace_path = :namespace_path
            ORDER BY table_name
            """;

    public static final String LIST_TABLE_NAMES_BY_NAMESPACE_PAGE = """
            SELECT id, table_name
            FROM kasanari_lance_tables
            WHERE catalog_name = :catalog_name
              AND namespace_path = :namespace_path
              AND id > :cursor_id
            ORDER BY id
            LIMIT :limit
            """;

    public static final String DELETE_TABLE = """
            DELETE FROM kasanari_lance_tables
            WHERE catalog_name = :catalog_name AND table_id = :table_id
            """;
}
