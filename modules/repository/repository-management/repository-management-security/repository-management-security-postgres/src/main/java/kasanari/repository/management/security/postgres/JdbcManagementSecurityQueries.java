package kasanari.repository.management.security.postgres;

public final class JdbcManagementSecurityQueries {
    private JdbcManagementSecurityQueries() {
    }

    public static final String CREATE_ROLE_BINDINGS_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_role_bindings
            (
                subject    TEXT                     NOT NULL,
                role_name  TEXT                     NOT NULL,
                resource   TEXT                     NOT NULL,
                created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_role_bindings_pk PRIMARY KEY (subject, role_name, resource)
            )
            """;

    public static final String UPSERT_ROLE_BINDING = """
            INSERT INTO kasanari_role_bindings(subject, role_name, resource)
            VALUES (?, ?, ?)
            ON CONFLICT (subject, role_name, resource) DO UPDATE SET
            updated_at = CURRENT_TIMESTAMP
            """;

    public static final String DELETE_ROLE_BINDING = """
            DELETE FROM kasanari_role_bindings
            WHERE subject = ? AND role_name = ? AND resource = ?
            """;

    public static final String SELECT_ROLE_BINDINGS = """
            SELECT subject, role_name, resource
            FROM kasanari_role_bindings
            WHERE (? IS NULL OR subject = ?)
            AND (? IS NULL OR resource LIKE ?)
            ORDER BY subject, role_name, resource
            """;
}
