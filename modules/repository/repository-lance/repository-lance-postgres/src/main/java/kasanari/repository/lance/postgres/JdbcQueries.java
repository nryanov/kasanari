package kasanari.repository.lance.postgres;

public final class JdbcQueries {
    private JdbcQueries() {
    }

    public static final String CREATE_NAMESPACES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_lance_namespaces (
                namespace_path TEXT PRIMARY KEY,
                properties TEXT NOT NULL
            )
            """;

    public static final String CREATE_TABLES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_lance_tables (
                table_id TEXT PRIMARY KEY,
                namespace_path TEXT NOT NULL,
                table_name TEXT NOT NULL,
                location TEXT,
                properties TEXT NOT NULL,
                declared_only BOOLEAN NOT NULL
            )
            """;

    public static final String CREATE_TABLE_VERSIONS_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_lance_table_versions (
                table_id TEXT NOT NULL,
                version BIGINT NOT NULL,
                manifest_path TEXT NOT NULL,
                manifest_size BIGINT,
                etag TEXT,
                metadata TEXT NOT NULL,
                timestamp_millis BIGINT NOT NULL,
                PRIMARY KEY (table_id, version)
            )
            """;

    public static final String CREATE_TRANSACTIONS_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_lance_transactions (
                transaction_id TEXT PRIMARY KEY,
                status TEXT NOT NULL,
                properties TEXT NOT NULL
            )
            """;
}
