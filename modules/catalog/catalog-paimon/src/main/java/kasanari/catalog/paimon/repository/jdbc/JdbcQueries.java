package kasanari.catalog.paimon.repository.jdbc;

public final class JdbcQueries {
    private JdbcQueries() {
    }

    public static final String CREATE_DATABASES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_paimon_databases
            (
                catalog_key     TEXT,
                database_name   TEXT,
                options_payload JSONB                    NOT NULL,
                comment         TEXT,
                created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_paimon_databases_pk PRIMARY KEY (catalog_key, database_name)
            )
            """;

    public static final String CREATE_TABLES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_paimon_tables
            (
                catalog_key       TEXT,
                database_name     TEXT,
                table_name        TEXT,
                properties_payload JSONB                    NOT NULL,
                created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_paimon_tables_pk PRIMARY KEY (catalog_key, database_name, table_name),
                CONSTRAINT kasanari_paimon_tables_db_fk FOREIGN KEY (catalog_key, database_name)
                    REFERENCES kasanari_paimon_databases (catalog_key, database_name) ON DELETE CASCADE
            )
            """;

    public static final String CREATE_VIEWS_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_paimon_views
            (
                catalog_key      TEXT,
                database_name    TEXT,
                view_name        TEXT,
                query            TEXT                     NOT NULL,
                dialects_payload JSONB                    NOT NULL,
                options_payload  JSONB                    NOT NULL,
                comment          TEXT,
                created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_paimon_views_pk PRIMARY KEY (catalog_key, database_name, view_name),
                CONSTRAINT kasanari_paimon_views_db_fk FOREIGN KEY (catalog_key, database_name)
                    REFERENCES kasanari_paimon_databases (catalog_key, database_name) ON DELETE CASCADE
            )
            """;

    public static final String CREATE_FUNCTIONS_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_paimon_functions
            (
                catalog_key         TEXT,
                database_name       TEXT,
                function_name       TEXT,
                deterministic       BOOLEAN                  NOT NULL,
                definitions_payload JSONB                    NOT NULL,
                options_payload     JSONB                    NOT NULL,
                comment             TEXT,
                created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_paimon_functions_pk PRIMARY KEY (catalog_key, database_name, function_name),
                CONSTRAINT kasanari_paimon_functions_db_fk FOREIGN KEY (catalog_key, database_name)
                    REFERENCES kasanari_paimon_databases (catalog_key, database_name) ON DELETE CASCADE
            )
            """;

    public static final String CREATE_BRANCHES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_paimon_branches
            (
                catalog_key   TEXT,
                database_name TEXT,
                table_name    TEXT,
                branch_name   TEXT,
                tag_name      TEXT,
                created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_paimon_branches_pk PRIMARY KEY (catalog_key, database_name, table_name, branch_name),
                CONSTRAINT kasanari_paimon_branches_table_fk FOREIGN KEY (catalog_key, database_name, table_name)
                    REFERENCES kasanari_paimon_tables (catalog_key, database_name, table_name)
                    ON UPDATE CASCADE ON DELETE CASCADE
            )
            """;

    public static final String CREATE_TAGS_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_paimon_tags
            (
                catalog_key       TEXT,
                database_name     TEXT,
                table_name        TEXT,
                tag_name          TEXT,
                snapshot_id       BIGINT                   NOT NULL,
                tag_create_time   BIGINT,
                tag_time_retained TEXT,
                created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_paimon_tags_pk PRIMARY KEY (catalog_key, database_name, table_name, tag_name),
                CONSTRAINT kasanari_paimon_tags_table_fk FOREIGN KEY (catalog_key, database_name, table_name)
                    REFERENCES kasanari_paimon_tables (catalog_key, database_name, table_name)
                    ON UPDATE CASCADE ON DELETE CASCADE
            )
            """;

    public static final String SELECT_DATABASE = """
            SELECT database_name, options_payload, comment
            FROM kasanari_paimon_databases
            WHERE catalog_key = ? AND database_name = ?
            """;
    public static final String INSERT_DATABASE = """
            INSERT INTO kasanari_paimon_databases(catalog_key, database_name, options_payload, comment)
            VALUES (?, ?, ?::jsonb, ?)
            """;
    public static final String DELETE_DATABASE = """
            DELETE FROM kasanari_paimon_databases
            WHERE catalog_key = ? AND database_name = ?
            """;
    public static final String UPDATE_DATABASE = """
            UPDATE kasanari_paimon_databases
            SET options_payload = ?::jsonb, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_key = ? AND database_name = ?
            """;
    public static final String LIST_DATABASES = """
            SELECT database_name, options_payload, comment
            FROM kasanari_paimon_databases
            WHERE catalog_key = ?
            ORDER BY database_name
            """;

    public static final String LIST_TABLES = """
            SELECT table_name, properties_payload
            FROM kasanari_paimon_tables
            WHERE catalog_key = ? AND database_name = ?
            ORDER BY table_name
            """;
    public static final String INSERT_TABLE = """
            INSERT INTO kasanari_paimon_tables(catalog_key, database_name, table_name, properties_payload)
            VALUES (?, ?, ?, ?::jsonb)
            """;
    public static final String UPDATE_TABLE = """
            UPDATE kasanari_paimon_tables
            SET properties_payload = ?::jsonb, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_key = ? AND database_name = ? AND table_name = ?
            """;
    public static final String DELETE_TABLE = """
            DELETE FROM kasanari_paimon_tables
            WHERE catalog_key = ? AND database_name = ? AND table_name = ?
            """;
    public static final String RENAME_TABLE = """
            UPDATE kasanari_paimon_tables
            SET database_name = ?, table_name = ?, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_key = ? AND database_name = ? AND table_name = ?
            """;
    public static final String CHECK_TABLE_EXISTS = """
            SELECT EXISTS(
                SELECT 1 FROM kasanari_paimon_tables
                WHERE catalog_key = ? AND database_name = ? AND table_name = ?
            )
            """;

    public static final String LIST_VIEWS = """
            SELECT view_name, query, dialects_payload, options_payload, comment
            FROM kasanari_paimon_views
            WHERE catalog_key = ? AND database_name = ?
            ORDER BY view_name
            """;
    public static final String SELECT_VIEW = """
            SELECT view_name, query, dialects_payload, options_payload, comment
            FROM kasanari_paimon_views
            WHERE catalog_key = ? AND database_name = ? AND view_name = ?
            """;
    public static final String INSERT_VIEW = """
            INSERT INTO kasanari_paimon_views(catalog_key, database_name, view_name, query, dialects_payload, options_payload, comment)
            VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?)
            """;
    public static final String UPDATE_VIEW = """
            UPDATE kasanari_paimon_views
            SET query = ?, dialects_payload = ?::jsonb, options_payload = ?::jsonb, comment = ?, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_key = ? AND database_name = ? AND view_name = ?
            """;
    public static final String DELETE_VIEW = """
            DELETE FROM kasanari_paimon_views
            WHERE catalog_key = ? AND database_name = ? AND view_name = ?
            """;
    public static final String RENAME_VIEW = """
            UPDATE kasanari_paimon_views
            SET database_name = ?, view_name = ?, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_key = ? AND database_name = ? AND view_name = ?
            """;

    public static final String LIST_FUNCTIONS = """
            SELECT function_name, deterministic, definitions_payload, options_payload, comment
            FROM kasanari_paimon_functions
            WHERE catalog_key = ? AND database_name = ?
            ORDER BY function_name
            """;
    public static final String SELECT_FUNCTION = """
            SELECT function_name, deterministic, definitions_payload, options_payload, comment
            FROM kasanari_paimon_functions
            WHERE catalog_key = ? AND database_name = ? AND function_name = ?
            """;
    public static final String INSERT_FUNCTION = """
            INSERT INTO kasanari_paimon_functions(catalog_key, database_name, function_name, deterministic, definitions_payload, options_payload, comment)
            VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?)
            """;
    public static final String UPDATE_FUNCTION = """
            UPDATE kasanari_paimon_functions
            SET deterministic = ?, definitions_payload = ?::jsonb, options_payload = ?::jsonb, comment = ?, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_key = ? AND database_name = ? AND function_name = ?
            """;
    public static final String DELETE_FUNCTION = """
            DELETE FROM kasanari_paimon_functions
            WHERE catalog_key = ? AND database_name = ? AND function_name = ?
            """;

    public static final String INSERT_BRANCH = """
            INSERT INTO kasanari_paimon_branches(catalog_key, database_name, table_name, branch_name, tag_name)
            VALUES (?, ?, ?, ?, ?)
            """;
    public static final String DELETE_BRANCH = """
            DELETE FROM kasanari_paimon_branches
            WHERE catalog_key = ? AND database_name = ? AND table_name = ? AND branch_name = ?
            """;
    public static final String RENAME_BRANCH = """
            UPDATE kasanari_paimon_branches
            SET branch_name = ?, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_key = ? AND database_name = ? AND table_name = ? AND branch_name = ?
            """;
    public static final String FAST_FORWARD_BRANCH = """
            UPDATE kasanari_paimon_branches
            SET tag_name = NULL, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_key = ? AND database_name = ? AND table_name = ? AND branch_name = ?
            """;
    public static final String LIST_BRANCHES = """
            SELECT branch_name, tag_name
            FROM kasanari_paimon_branches
            WHERE catalog_key = ? AND database_name = ? AND table_name = ?
            ORDER BY branch_name
            """;

    public static final String INSERT_TAG = """
            INSERT INTO kasanari_paimon_tags(catalog_key, database_name, table_name, tag_name, snapshot_id, tag_create_time, tag_time_retained)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    public static final String INSERT_TAG_IGNORE_IF_EXISTS = """
            INSERT INTO kasanari_paimon_tags(catalog_key, database_name, table_name, tag_name, snapshot_id, tag_create_time, tag_time_retained)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (catalog_key, database_name, table_name, tag_name) DO NOTHING
            """;
    public static final String DELETE_TAG = """
            DELETE FROM kasanari_paimon_tags
            WHERE catalog_key = ? AND database_name = ? AND table_name = ? AND tag_name = ?
            """;
    public static final String SELECT_TAG = """
            SELECT tag_name, snapshot_id, tag_create_time, tag_time_retained
            FROM kasanari_paimon_tags
            WHERE catalog_key = ? AND database_name = ? AND table_name = ? AND tag_name = ?
            """;
    public static final String CHECK_TAG_EXISTS = """
            SELECT EXISTS(
                SELECT 1 FROM kasanari_paimon_tags
                WHERE catalog_key = ? AND database_name = ? AND table_name = ? AND tag_name = ?
            )
            """;
    public static final String LIST_TAGS = """
            SELECT tag_name
            FROM kasanari_paimon_tags
            WHERE catalog_key = ? AND database_name = ? AND table_name = ?
            ORDER BY tag_name
            """;
    public static final String LIST_TAGS_WITH_PREFIX = """
            SELECT tag_name
            FROM kasanari_paimon_tags
            WHERE catalog_key = ? AND database_name = ? AND table_name = ? AND tag_name LIKE ?
            ORDER BY tag_name
            """;

    public static final String ACQUIRE_TRANSACTIONAL_ADVISORY_LOCK = "SELECT pg_advisory_xact_lock(?)";
}
