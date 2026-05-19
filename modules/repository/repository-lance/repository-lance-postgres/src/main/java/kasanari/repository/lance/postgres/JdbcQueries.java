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
                properties JSONB NOT NULL DEFAULT '{}'::jsonb,
                declared_only BOOLEAN NOT NULL
            )
            """;
}
