package kasanari.repository.management.security.postgres;

public final class JdbcManagementSecurityQueries {
    private JdbcManagementSecurityQueries() {
    }

    public static final String CREATE_ROLE_BINDINGS_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_role_bindings
            (
                subject    TEXT                     NOT NULL,
                resource   TEXT                     NOT NULL,
                role       TEXT                     NOT NULL,
                created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_role_bindings_pk PRIMARY KEY (subject, resource, role)
            )
            """;

    public static final String INSERT_ROLE_BINDING = """
            INSERT INTO kasanari_role_bindings(subject, resource, role)
            VALUES (?, ?, ?)
            ON CONFLICT (subject, resource, role) DO NOTHING
            """;

    public static final String DELETE_ROLE_BINDING = """
            DELETE FROM kasanari_role_bindings
            WHERE subject = ? AND resource = ? AND role = ?
            """;

    public static final String SELECT_ROLE_BINDINGS = """
            SELECT subject, resource, role
            FROM kasanari_role_bindings
            WHERE resource = ?
            AND (? IS NULL OR subject = ?)
            ORDER BY subject, role, resource
            """;

    public static final String SELECT_ALL_ROLE_BINDINGS = """
            SELECT subject, resource, role
            FROM kasanari_role_bindings
            ORDER BY subject, role, resource
            """;
}
