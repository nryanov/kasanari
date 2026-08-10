package kasanari.repository.paimon.yugabyte;

public final class JdbcQueries {
    private JdbcQueries() {
    }

    public static final String CREATE_DATABASES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_paimon_databases
            (
                catalog_name     TEXT,
                database_name   TEXT,
                options_payload JSONB                    NOT NULL,
                comment         TEXT,
                created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_paimon_databases_pk PRIMARY KEY (catalog_name HASH, database_name ASC)
            ) WITH (colocation = false)
            """;

    public static final String CREATE_TABLES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_paimon_tables
            (
                id                BIGINT GENERATED ALWAYS AS IDENTITY,
                catalog_name       TEXT,
                database_name     TEXT,
                table_name        TEXT,
                table_uuid        TEXT,
                properties_payload JSONB                    NOT NULL,
                created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_paimon_tables_id_uq UNIQUE (id),
                CONSTRAINT kasanari_paimon_tables_pk PRIMARY KEY (catalog_name HASH, database_name ASC, table_name ASC),
                CONSTRAINT kasanari_paimon_tables_db_fk FOREIGN KEY (catalog_name, database_name)
                    REFERENCES kasanari_paimon_databases (catalog_name, database_name) ON DELETE CASCADE
            ) WITH (colocation = false)
            """;

    public static final String CREATE_PARTITION_STATS_DELTAS_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_paimon_partition_stats_deltas
            (
                catalog_name             TEXT,
                database_name           TEXT,
                table_name              TEXT,
                branch_name             TEXT,
                snapshot_id             BIGINT                   NOT NULL,
                spec_hash               TEXT,
                spec_payload            JSONB                    NOT NULL,
                record_count_delta      BIGINT                   NOT NULL,
                file_size_delta         BIGINT                   NOT NULL,
                file_count_delta        BIGINT                   NOT NULL,
                last_file_creation_time BIGINT                   NOT NULL,
                total_buckets           INT                      NOT NULL,
                created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_paimon_partition_stats_deltas_pk PRIMARY KEY (catalog_name HASH, database_name ASC, table_name ASC, branch_name ASC, snapshot_id ASC, spec_hash ASC),
                CONSTRAINT kasanari_paimon_partition_stats_deltas_table_fk FOREIGN KEY (catalog_name, database_name, table_name)
                    REFERENCES kasanari_paimon_tables (catalog_name, database_name, table_name)
                    ON UPDATE CASCADE ON DELETE CASCADE
            ) WITH (colocation = false)
            """;

    public static final String CREATE_PARTITION_STATES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_paimon_partition_states
            (
                id                      BIGINT GENERATED ALWAYS AS IDENTITY,
                catalog_name             TEXT,
                database_name           TEXT,
                table_name              TEXT,
                branch_name             TEXT,
                spec_hash               TEXT,
                spec_payload            JSONB                    NOT NULL,
                record_count            BIGINT                   NOT NULL,
                file_size_in_bytes      BIGINT                   NOT NULL,
                file_count              BIGINT                   NOT NULL,
                last_file_creation_time BIGINT                   NOT NULL,
                total_buckets           INT                      NOT NULL,
                done                    BOOLEAN                  NOT NULL DEFAULT FALSE,
                options_payload         JSONB                    NOT NULL DEFAULT '{}'::jsonb,
                created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_paimon_partition_states_id_uq UNIQUE (id),
                CONSTRAINT kasanari_paimon_partition_states_pk PRIMARY KEY (
                    catalog_name HASH, database_name ASC, table_name ASC, branch_name ASC, spec_hash ASC
                ),
                CONSTRAINT kasanari_paimon_partition_states_table_fk FOREIGN KEY (catalog_name, database_name, table_name)
                    REFERENCES kasanari_paimon_tables (catalog_name, database_name, table_name)
                    ON UPDATE CASCADE ON DELETE CASCADE
            ) WITH (colocation = false)
            """;

    public static final String CREATE_VIEWS_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_paimon_views
            (
                id               BIGINT GENERATED ALWAYS AS IDENTITY,
                catalog_name      TEXT,
                database_name    TEXT,
                view_name        TEXT,
                query            TEXT                     NOT NULL,
                dialects_payload JSONB                    NOT NULL,
                options_payload  JSONB                    NOT NULL,
                comment          TEXT,
                created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_paimon_views_id_uq UNIQUE (id),
                CONSTRAINT kasanari_paimon_views_pk PRIMARY KEY (catalog_name HASH, database_name ASC, view_name ASC),
                CONSTRAINT kasanari_paimon_views_db_fk FOREIGN KEY (catalog_name, database_name)
                    REFERENCES kasanari_paimon_databases (catalog_name, database_name) ON DELETE CASCADE
            ) WITH (colocation = false)
            """;

    public static final String CREATE_FUNCTIONS_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_paimon_functions
            (
                id                  BIGINT GENERATED ALWAYS AS IDENTITY,
                catalog_name         TEXT,
                database_name       TEXT,
                function_name       TEXT,
                deterministic       BOOLEAN                  NOT NULL,
                definitions_payload JSONB                    NOT NULL,
                options_payload     JSONB                    NOT NULL,
                comment             TEXT,
                created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_paimon_functions_id_uq UNIQUE (id),
                CONSTRAINT kasanari_paimon_functions_pk PRIMARY KEY (catalog_name HASH, database_name ASC, function_name ASC),
                CONSTRAINT kasanari_paimon_functions_db_fk FOREIGN KEY (catalog_name, database_name)
                    REFERENCES kasanari_paimon_databases (catalog_name, database_name) ON DELETE CASCADE
            ) WITH (colocation = false)
            """;

    public static final String CREATE_BRANCHES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_paimon_branches
            (
                catalog_name   TEXT,
                database_name TEXT,
                table_name    TEXT,
                branch_name   TEXT,
                tag_name      TEXT,
                created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_paimon_branches_pk PRIMARY KEY (catalog_name HASH, database_name ASC, table_name ASC, branch_name ASC),
                CONSTRAINT kasanari_paimon_branches_table_fk FOREIGN KEY (catalog_name, database_name, table_name)
                    REFERENCES kasanari_paimon_tables (catalog_name, database_name, table_name)
                    ON UPDATE CASCADE ON DELETE CASCADE
            ) WITH (colocation = false)
            """;

    public static final String CREATE_TAGS_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_paimon_tags
            (
                id                BIGINT GENERATED ALWAYS AS IDENTITY,
                catalog_name       TEXT,
                database_name     TEXT,
                table_name        TEXT,
                tag_name          TEXT,
                snapshot_id       BIGINT                   NOT NULL,
                tag_create_time   BIGINT,
                tag_time_retained TEXT,
                created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_paimon_tags_id_uq UNIQUE (id),
                CONSTRAINT kasanari_paimon_tags_pk PRIMARY KEY (catalog_name HASH, database_name ASC, table_name ASC, tag_name ASC),
                CONSTRAINT kasanari_paimon_tags_table_fk FOREIGN KEY (catalog_name, database_name, table_name)
                    REFERENCES kasanari_paimon_tables (catalog_name, database_name, table_name)
                    ON UPDATE CASCADE ON DELETE CASCADE
            ) WITH (colocation = false)
            """;

    public static final String SELECT_DATABASE = """
            SELECT database_name, options_payload, comment
            FROM kasanari_paimon_databases
            WHERE catalog_name = ? AND database_name = ?
            """;
    public static final String INSERT_DATABASE = """
            INSERT INTO kasanari_paimon_databases(catalog_name, database_name, options_payload, comment)
            VALUES (?, ?, ?::jsonb, ?)
            """;
    public static final String DELETE_DATABASE = """
            DELETE FROM kasanari_paimon_databases
            WHERE catalog_name = ? AND database_name = ?
            """;
    public static final String UPDATE_DATABASE = """
            UPDATE kasanari_paimon_databases
            SET options_payload = ?::jsonb, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_name = ? AND database_name = ?
            """;
    public static final String LIST_DATABASES = """
            SELECT database_name, options_payload, comment
            FROM kasanari_paimon_databases
            WHERE catalog_name = ?
            ORDER BY database_name
            """;

    public static final String LIST_TABLES = """
            SELECT table_name, properties_payload, table_uuid
            FROM kasanari_paimon_tables
            WHERE catalog_name = ? AND database_name = ?
            ORDER BY table_name
            """;
    public static final String LIST_TABLES_PAGE = """
            SELECT id, table_name, properties_payload, table_uuid
            FROM kasanari_paimon_tables
            WHERE catalog_name = ? AND database_name = ? AND id > ? AND table_name LIKE ?
            ORDER BY id
            LIMIT ?
            """;
    public static final String LIST_TABLES_PAGE_GLOBALLY = """
            SELECT id, database_name, table_name, properties_payload, table_uuid
            FROM kasanari_paimon_tables
            WHERE catalog_name = ? AND id > ? AND database_name LIKE ? AND table_name LIKE ?
            ORDER BY id
            LIMIT ?
            """;
    public static final String INSERT_TABLE = """
            INSERT INTO kasanari_paimon_tables(catalog_name, database_name, table_name, table_uuid, properties_payload)
            VALUES (?, ?, ?, ?, ?::jsonb)
            """;
    public static final String UPDATE_TABLE = """
            UPDATE kasanari_paimon_tables
            SET properties_payload = ?::jsonb, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_name = ? AND database_name = ? AND table_name = ?
            """;
    public static final String DELETE_TABLE = """
            DELETE FROM kasanari_paimon_tables
            WHERE catalog_name = ? AND database_name = ? AND table_name = ?
            """;
    public static final String RENAME_TABLE = """
            UPDATE kasanari_paimon_tables
            SET database_name = ?, table_name = ?, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_name = ? AND database_name = ? AND table_name = ?
            """;
    public static final String CHECK_TABLE_EXISTS = """
            SELECT EXISTS(
                SELECT 1 FROM kasanari_paimon_tables
                WHERE catalog_name = ? AND database_name = ? AND table_name = ?
            )
            """;
    public static final String SELECT_TABLE = """
            SELECT table_name, properties_payload, table_uuid
            FROM kasanari_paimon_tables
            WHERE catalog_name = ? AND database_name = ? AND table_name = ?
            """;
    public static final String SELECT_TABLE_BY_UUID = """
            SELECT database_name, table_name, properties_payload, table_uuid
            FROM kasanari_paimon_tables
            WHERE catalog_name = ? AND table_uuid = ?
            """;
    public static final String UPSERT_PARTITION_STATE = """
            INSERT INTO kasanari_paimon_partition_states(
                catalog_name,
                database_name,
                table_name,
                branch_name,
                spec_hash,
                spec_payload,
                record_count,
                file_size_in_bytes,
                file_count,
                last_file_creation_time,
                total_buckets,
                done,
                options_payload
            )
            VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?, FALSE, ?::jsonb)
            ON CONFLICT (catalog_name, database_name, table_name, branch_name, spec_hash) DO UPDATE SET
                record_count = kasanari_paimon_partition_states.record_count + EXCLUDED.record_count,
                file_size_in_bytes = kasanari_paimon_partition_states.file_size_in_bytes + EXCLUDED.file_size_in_bytes,
                file_count = kasanari_paimon_partition_states.file_count + EXCLUDED.file_count,
                last_file_creation_time = GREATEST(
                    kasanari_paimon_partition_states.last_file_creation_time,
                    EXCLUDED.last_file_creation_time
                ),
                total_buckets = EXCLUDED.total_buckets,
                done = FALSE,
                updated_at = CURRENT_TIMESTAMP
            """;
    public static final String DELETE_EMPTY_PARTITION_STATES = """
            DELETE FROM kasanari_paimon_partition_states
            WHERE catalog_name = ? AND database_name = ? AND table_name = ? AND branch_name = ? AND file_count <= 0
            """;
    public static final String INSERT_PARTITION_STATS_DELTA = """
            INSERT INTO kasanari_paimon_partition_stats_deltas(
                catalog_name,
                database_name,
                table_name,
                branch_name,
                snapshot_id,
                spec_hash,
                spec_payload,
                record_count_delta,
                file_size_delta,
                file_count_delta,
                last_file_creation_time,
                total_buckets
            )
            VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?)
            ON CONFLICT (catalog_name, database_name, table_name, branch_name, snapshot_id, spec_hash) DO NOTHING
            """;
    public static final String LIST_PARTITION_STATES = """
            SELECT spec_payload, record_count, file_size_in_bytes, file_count, last_file_creation_time, total_buckets, done, options_payload
            FROM kasanari_paimon_partition_states
            WHERE catalog_name = ? AND database_name = ? AND table_name = ? AND branch_name = ?
            ORDER BY spec_hash
            """;
    public static final String LIST_PARTITION_STATES_PAGE = """
            SELECT id, spec_payload, record_count, file_size_in_bytes, file_count, last_file_creation_time, total_buckets, done, options_payload
            FROM kasanari_paimon_partition_states
            WHERE catalog_name = ? AND database_name = ? AND table_name = ? AND branch_name = ? AND id > ?
            ORDER BY id
            LIMIT ?
            """;
    public static final String MARK_DONE_PARTITION_STATE = """
            UPDATE kasanari_paimon_partition_states
            SET done = TRUE, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_name = ? AND database_name = ? AND table_name = ? AND branch_name = ? AND spec_hash = ?
            """;
    public static final String INSERT_PARTITION_STATE_IF_ABSENT = """
            INSERT INTO kasanari_paimon_partition_states(
                catalog_name,
                database_name,
                table_name,
                branch_name,
                spec_hash,
                spec_payload,
                record_count,
                file_size_in_bytes,
                file_count,
                last_file_creation_time,
                total_buckets,
                done,
                options_payload
            )
            VALUES (?, ?, ?, ?, ?, ?::jsonb, 0, 0, 0, 0, 0, FALSE, '{}'::jsonb)
            ON CONFLICT (catalog_name, database_name, table_name, branch_name, spec_hash) DO NOTHING
            """;
    public static final String DELETE_PARTITION_STATE_BY_HASH = """
            DELETE FROM kasanari_paimon_partition_states
            WHERE catalog_name = ? AND database_name = ? AND table_name = ? AND branch_name = ? AND spec_hash = ?
            """;
    public static final String UPSERT_PARTITION_STATE_ABSOLUTE = """
            INSERT INTO kasanari_paimon_partition_states(
                catalog_name,
                database_name,
                table_name,
                branch_name,
                spec_hash,
                spec_payload,
                record_count,
                file_size_in_bytes,
                file_count,
                last_file_creation_time,
                total_buckets,
                done,
                options_payload
            )
            VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?, FALSE, '{}'::jsonb)
            ON CONFLICT (catalog_name, database_name, table_name, branch_name, spec_hash) DO UPDATE SET
                spec_payload = EXCLUDED.spec_payload,
                record_count = EXCLUDED.record_count,
                file_size_in_bytes = EXCLUDED.file_size_in_bytes,
                file_count = EXCLUDED.file_count,
                last_file_creation_time = EXCLUDED.last_file_creation_time,
                total_buckets = EXCLUDED.total_buckets,
                done = FALSE,
                updated_at = CURRENT_TIMESTAMP
            """;

    public static final String LIST_VIEWS = """
            SELECT view_name, query, dialects_payload, options_payload, comment
            FROM kasanari_paimon_views
            WHERE catalog_name = ? AND database_name = ?
            ORDER BY view_name
            """;
    public static final String LIST_VIEWS_PAGE = """
            SELECT id, view_name, query, dialects_payload, options_payload, comment
            FROM kasanari_paimon_views
            WHERE catalog_name = ? AND database_name = ? AND id > ? AND view_name LIKE ?
            ORDER BY id
            LIMIT ?
            """;
    public static final String LIST_VIEWS_PAGE_GLOBALLY = """
            SELECT id, database_name, view_name, query, dialects_payload, options_payload, comment
            FROM kasanari_paimon_views
            WHERE catalog_name = ? AND id > ? AND database_name LIKE ? AND view_name LIKE ?
            ORDER BY id
            LIMIT ?
            """;
    public static final String SELECT_VIEW = """
            SELECT view_name, query, dialects_payload, options_payload, comment
            FROM kasanari_paimon_views
            WHERE catalog_name = ? AND database_name = ? AND view_name = ?
            """;
    public static final String INSERT_VIEW = """
            INSERT INTO kasanari_paimon_views(catalog_name, database_name, view_name, query, dialects_payload, options_payload, comment)
            VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?)
            """;
    public static final String UPDATE_VIEW = """
            UPDATE kasanari_paimon_views
            SET query = ?, dialects_payload = ?::jsonb, options_payload = ?::jsonb, comment = ?, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_name = ? AND database_name = ? AND view_name = ?
            """;
    public static final String DELETE_VIEW = """
            DELETE FROM kasanari_paimon_views
            WHERE catalog_name = ? AND database_name = ? AND view_name = ?
            """;
    public static final String RENAME_VIEW = """
            UPDATE kasanari_paimon_views
            SET database_name = ?, view_name = ?, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_name = ? AND database_name = ? AND view_name = ?
            """;

    public static final String LIST_FUNCTIONS = """
            SELECT function_name, deterministic, definitions_payload, options_payload, comment
            FROM kasanari_paimon_functions
            WHERE catalog_name = ? AND database_name = ?
            ORDER BY function_name
            """;
    public static final String LIST_FUNCTIONS_PAGE = """
            SELECT id, function_name, deterministic, definitions_payload, options_payload, comment
            FROM kasanari_paimon_functions
            WHERE catalog_name = ? AND database_name = ? AND id > ? AND function_name LIKE ?
            ORDER BY id
            LIMIT ?
            """;
    public static final String LIST_FUNCTIONS_PAGE_GLOBALLY = """
            SELECT id, database_name, function_name, deterministic, definitions_payload, options_payload, comment
            FROM kasanari_paimon_functions
            WHERE catalog_name = ? AND id > ? AND database_name LIKE ? AND function_name LIKE ?
            ORDER BY id
            LIMIT ?
            """;
    public static final String SELECT_FUNCTION = """
            SELECT function_name, deterministic, definitions_payload, options_payload, comment
            FROM kasanari_paimon_functions
            WHERE catalog_name = ? AND database_name = ? AND function_name = ?
            """;
    public static final String INSERT_FUNCTION = """
            INSERT INTO kasanari_paimon_functions(catalog_name, database_name, function_name, deterministic, definitions_payload, options_payload, comment)
            VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?)
            """;
    public static final String UPDATE_FUNCTION = """
            UPDATE kasanari_paimon_functions
            SET deterministic = ?, definitions_payload = ?::jsonb, options_payload = ?::jsonb, comment = ?, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_name = ? AND database_name = ? AND function_name = ?
            """;
    public static final String DELETE_FUNCTION = """
            DELETE FROM kasanari_paimon_functions
            WHERE catalog_name = ? AND database_name = ? AND function_name = ?
            """;

    public static final String INSERT_BRANCH = """
            INSERT INTO kasanari_paimon_branches(catalog_name, database_name, table_name, branch_name, tag_name)
            VALUES (?, ?, ?, ?, ?)
            """;
    public static final String DELETE_BRANCH = """
            DELETE FROM kasanari_paimon_branches
            WHERE catalog_name = ? AND database_name = ? AND table_name = ? AND branch_name = ?
            """;
    public static final String RENAME_BRANCH = """
            UPDATE kasanari_paimon_branches
            SET branch_name = ?, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_name = ? AND database_name = ? AND table_name = ? AND branch_name = ?
            """;
    public static final String FAST_FORWARD_BRANCH = """
            UPDATE kasanari_paimon_branches
            SET tag_name = NULL, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_name = ? AND database_name = ? AND table_name = ? AND branch_name = ?
            """;
    public static final String LIST_BRANCHES = """
            SELECT branch_name, tag_name
            FROM kasanari_paimon_branches
            WHERE catalog_name = ? AND database_name = ? AND table_name = ?
            ORDER BY branch_name
            """;

    public static final String INSERT_TAG = """
            INSERT INTO kasanari_paimon_tags(catalog_name, database_name, table_name, tag_name, snapshot_id, tag_create_time, tag_time_retained)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    public static final String INSERT_TAG_IGNORE_IF_EXISTS = """
            INSERT INTO kasanari_paimon_tags(catalog_name, database_name, table_name, tag_name, snapshot_id, tag_create_time, tag_time_retained)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (catalog_name, database_name, table_name, tag_name) DO NOTHING
            """;
    public static final String DELETE_TAG = """
            DELETE FROM kasanari_paimon_tags
            WHERE catalog_name = ? AND database_name = ? AND table_name = ? AND tag_name = ?
            """;
    public static final String SELECT_TAG = """
            SELECT tag_name, snapshot_id, tag_create_time, tag_time_retained
            FROM kasanari_paimon_tags
            WHERE catalog_name = ? AND database_name = ? AND table_name = ? AND tag_name = ?
            """;
    public static final String CHECK_TAG_EXISTS = """
            SELECT EXISTS(
                SELECT 1 FROM kasanari_paimon_tags
                WHERE catalog_name = ? AND database_name = ? AND table_name = ? AND tag_name = ?
            )
            """;
    public static final String LIST_TAGS = """
            SELECT tag_name
            FROM kasanari_paimon_tags
            WHERE catalog_name = ? AND database_name = ? AND table_name = ?
            ORDER BY tag_name
            """;
    public static final String LIST_TAGS_WITH_PREFIX = """
            SELECT tag_name
            FROM kasanari_paimon_tags
            WHERE catalog_name = ? AND database_name = ? AND table_name = ? AND tag_name LIKE ?
            ORDER BY tag_name
            """;
    public static final String LIST_TAGS_PAGE = """
            SELECT id, tag_name, snapshot_id, tag_create_time, tag_time_retained
            FROM kasanari_paimon_tags
            WHERE catalog_name = ? AND database_name = ? AND table_name = ? AND id > ? AND tag_name LIKE ?
            ORDER BY id
            LIMIT ?
            """;

    public static final String ACQUIRE_TRANSACTIONAL_ADVISORY_LOCK = "SELECT pg_advisory_xact_lock(hashtextextended(?, 0))";
}
