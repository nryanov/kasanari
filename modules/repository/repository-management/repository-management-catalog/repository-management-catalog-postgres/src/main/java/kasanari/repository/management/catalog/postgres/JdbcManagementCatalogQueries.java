package kasanari.repository.management.catalog.postgres;

public final class JdbcManagementCatalogQueries {
    private JdbcManagementCatalogQueries() {
    }

    public static final String CREATE_CATALOG_REGISTRY_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_catalogs
            (
                catalog_type TEXT                     NOT NULL,
                catalog_name TEXT                     NOT NULL,
                catalog_mode TEXT                     NOT NULL,
                spec_json    JSON                     NOT NULL,
                version      BIGINT                   NOT NULL DEFAULT 1,
                created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_catalogs_pk PRIMARY KEY (catalog_type, catalog_name)
            )
            """;

    public static final String INSERT_CATALOG = """
            INSERT INTO kasanari_catalogs(catalog_type, catalog_name, catalog_mode, spec_json, version)
            VALUES (?, ?, ?, ?::json, ?)
            """;

    public static final String UPDATE_CATALOG = """
            UPDATE kasanari_catalogs
            SET spec_json = ?::json, version = ?, updated_at = CURRENT_TIMESTAMP
            WHERE catalog_type = ? AND catalog_name = ? AND version = ?
            """;

    public static final String DELETE_CATALOG = """
            DELETE FROM kasanari_catalogs
            WHERE catalog_type = ? AND catalog_name = ?
            """;

    public static final String SELECT_CATALOG = """
            SELECT catalog_type, catalog_name, catalog_mode, spec_json, version
            FROM kasanari_catalogs
            WHERE catalog_type = ? AND catalog_name = ?
            """;
}
