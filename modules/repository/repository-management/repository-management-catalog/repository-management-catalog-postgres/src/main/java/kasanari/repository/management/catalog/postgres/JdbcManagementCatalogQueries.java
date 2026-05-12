package kasanari.repository.management.catalog.postgres;

public final class JdbcManagementCatalogQueries {
    private JdbcManagementCatalogQueries() {
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
}
