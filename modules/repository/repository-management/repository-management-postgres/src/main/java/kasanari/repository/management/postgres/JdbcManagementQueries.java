package kasanari.repository.management.postgres;

public final class JdbcManagementQueries {
    private JdbcManagementQueries() {
    }

    public static final String CREATE_CATALOG_REGISTRY_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_catalog_registry
            (
                catalog_id   TEXT PRIMARY KEY,
                catalog_type TEXT                     NOT NULL,
                catalog_mode TEXT                     NOT NULL,
                spec_json    TEXT                     NOT NULL,
                version      BIGINT                   NOT NULL DEFAULT 1,
                created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;

    public static final String CREATE_ROLE_BINDINGS_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_role_bindings
            (
                subject      TEXT                     NOT NULL,
                catalog_type TEXT                     NOT NULL,
                role_name    TEXT                     NOT NULL,
                created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_role_bindings_pk PRIMARY KEY (subject, catalog_type, role_name)
            )
            """;

    public static final String INSERT_CATALOG = """
            INSERT INTO kasanari_catalog_registry(catalog_id, catalog_type, catalog_mode, spec_json, version)
            VALUES (?, ?, ?, ?, ?)
            """;

    // todo: use CAS logic (version check)
    public static final String UPDATE_CATALOG = """
            UPDATE kasanari_catalog_registry
            SET spec_json = ?, version = ?, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_id = ?
            """;

    public static final String DELETE_CATALOG = "DELETE FROM kasanari_catalog_registry WHERE catalog_id = ?";

    public static final String SELECT_CATALOG = """
            SELECT catalog_id, catalog_type, catalog_mode, spec_json, version
            FROM kasanari_catalog_registry
            WHERE catalog_id = ?
            """;

    public static final String UPSERT_ROLE_BINDING = """
            INSERT INTO kasanari_role_bindings(subject, catalog_type, role_name)
            VALUES (?, ?, ?)
            ON CONFLICT (subject, catalog_type, role_name) DO UPDATE SET
            updated_at = CURRENT_TIMESTAMP
            """;

    public static final String DELETE_ROLE_BINDING = """
            DELETE FROM kasanari_role_bindings
            WHERE subject = ? AND catalog_type = ? AND role_name = ?
            """;

    public static final String SELECT_ROLE_BINDINGS = """
            SELECT subject, catalog_type, role_name
            FROM kasanari_role_bindings
            WHERE (? IS NULL OR subject = ?)
            AND (? IS NULL OR catalog_type = ?)
            ORDER BY subject, catalog_type, role_name
            """;
}
