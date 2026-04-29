package kasanari.catalog.iceberg.kasanari.repository.jdbc;

public class JdbcQueries {
    public final static String CREATE_CATALOGS_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_iceberg_catalog
            (
                catalog_name TEXT,
                created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_iceberg_catalog_pk PRIMARY KEY (catalog_name)
            )
            """;

    public final static String CREATE_NAMESPACES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_iceberg_namespaces
            (
                catalog_name   TEXT,
                namespace_name TEXT,
                created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_iceberg_namespaces_pk PRIMARY KEY (catalog_name, namespace_name),
                CONSTRAINT kasanari_iceberg_namespaces_catalog_fk FOREIGN KEY (catalog_name) REFERENCES kasanari_iceberg_catalog (catalog_name) ON DELETE CASCADE
            )
            """;

    public final static String CREATE_NAMESPACE_PROPERTIES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_iceberg_namespace_properties
            (
                catalog_name   TEXT,
                namespace_name TEXT,
                property_key   TEXT,
                property_value TEXT,
                created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_iceberg_namespace_properties_pk PRIMARY KEY (catalog_name, namespace_name, property_key),
                CONSTRAINT kasanari_iceberg_namespace_properties_catalog_fk FOREIGN KEY (catalog_name) REFERENCES kasanari_iceberg_catalog (catalog_name) ON DELETE CASCADE,
                CONSTRAINT kasanari_iceberg_namespace_properties_namespace_fk FOREIGN KEY (catalog_name, namespace_name) REFERENCES kasanari_iceberg_namespaces (catalog_name, namespace_name) ON DELETE CASCADE
            )
            """;

    public final static String CREATE_TABLES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_iceberg_tables
            (
                catalog_name               TEXT,
                namespace_name             TEXT,
                table_name                 TEXT,
                metadata_location          TEXT,
                previous_metadata_location TEXT,
                created_at                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_iceberg_tables_pk PRIMARY KEY (catalog_name, namespace_name, table_name),
                CONSTRAINT kasanari_iceberg_tables_catalog_fk FOREIGN KEY (catalog_name) REFERENCES kasanari_iceberg_catalog (catalog_name) ON DELETE CASCADE,
                CONSTRAINT kasanari_iceberg_tables_namespace_fk FOREIGN KEY (catalog_name, namespace_name) REFERENCES kasanari_iceberg_namespaces (catalog_name, namespace_name) ON DELETE CASCADE
            )
            """;

    public final static String CREATE_VIEWS_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_iceberg_views
            (
                catalog_name               TEXT,
                namespace_name             TEXT,
                view_name                  TEXT,
                metadata_location          TEXT,
                previous_metadata_location TEXT,
                created_at                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at                 TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT kasanari_iceberg_views_pk PRIMARY KEY (catalog_name, namespace_name, view_name),
                CONSTRAINT kasanari_iceberg_views_catalog_fk FOREIGN KEY (catalog_name) REFERENCES kasanari_iceberg_catalog (catalog_name) ON DELETE CASCADE,
                CONSTRAINT kasanari_iceberg_views_namespace_fk FOREIGN KEY (catalog_name, namespace_name) REFERENCES kasanari_iceberg_namespaces (catalog_name, namespace_name) ON DELETE CASCADE
            )
            """;

    // to avoid creation of additional tuples on upsert
    public final static String CHECK_IF_CATALOG_EXISTS = "SELECT EXISTS(SELECT 1 FROM kasanari_iceberg_catalog WHERE catalog_name = ?)";

    public final static String REGISTER_CATALOG = "INSERT INTO kasanari_iceberg_catalog(catalog_name) VALUES (?)";

    public final static String CHECK_IF_NAMESPACE_EXISTS = "SELECT EXISTS(SELECT 1 FROM kasanari_iceberg_namespaces WHERE catalog_name = ? AND namespace_name = ?)";

    public final static String CREATE_NAMESPACE = "INSERT INTO kasanari_iceberg_namespaces(catalog_name, namespace_name) VALUES (?, ?)";

    public final static String UPSERT_NAMESPACE_PROPERTIES = """
            INSERT INTO kasanari_iceberg_namespace_properties(catalog_name, namespace_name, property_key, property_value)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (catalog_name, namespace_name, property_key) DO UPDATE SET updated_at = CURRENT_TIMESTAMP, property_value = EXCLUDED.property_value
            """;

    public static String SELECT_ROOT_NAMESPACES = """
            SELECT namespace_name FROM kasanari_iceberg_namespaces
            WHERE catalog_name = ? AND POSITION('.' IN namespace_name) = 0
            ORDER BY namespace_name
            """;

    public static String SELECT_CHILD_NAMESPACES = """
            SELECT namespace_name FROM kasanari_iceberg_namespaces
            WHERE catalog_name = ? AND namespace_name ~ ?
            ORDER BY namespace_name
            """;

    public static String SELECT_NAMESPACE_PROPERTIES = """
            SELECT property_key, property_value FROM kasanari_iceberg_namespace_properties
            WHERE catalog_name = ? AND namespace_name = ?
            """;

    public static String CHECK_NAMESPACE_TABLES_RELATIONSHIPS = "SELECT EXISTS(SELECT 1 FROM kasanari_iceberg_tables WHERE catalog_name = ? AND namespace_name = ?)";

    public static String CHECK_NAMESPACE_VIEWS_RELATIONSHIPS = "SELECT EXISTS(SELECT 1 FROM kasanari_iceberg_views WHERE catalog_name = ? AND namespace_name = ?)";

    public static String DELETE_NAMESPACE = "DELETE FROM kasanari_iceberg_namespaces WHERE catalog_name = ? AND namespace_name = ?";

    public static String REMOVE_NAMESPACE_PROPERTIES = """
            DELETE FROM kasanari_iceberg_namespace_properties
            WHERE catalog_name = ? AND namespace_name = ?
            AND property_key = ANY(?)
            """;

    public static String SELECT_TABLE = """
            SELECT catalog_name, namespace_name, table_name, metadata_location, previous_metadata_location
            FROM kasanari_iceberg_tables WHERE catalog_name = ? AND namespace_name = ? AND table_name = ?
            """;

    public static String SELECT_VIEW = """
            SELECT catalog_name, namespace_name, view_name, metadata_location, previous_metadata_location
            FROM kasanari_iceberg_views WHERE catalog_name = ? AND namespace_name = ? AND view_name = ?
            """;

    public static String CHECK_IF_TABLE_EXISTS = "SELECT EXISTS(SELECT 1 FROM kasanari_iceberg_tables WHERE catalog_name = ? AND namespace_name = ? AND table_name = ?)";

    public static String CHECK_IF_VIEW_EXISTS = "SELECT EXISTS(SELECT 1 FROM kasanari_iceberg_views WHERE catalog_name = ? AND namespace_name = ? AND view_name = ?)";

    public static String CREATE_TABLE = """
            INSERT INTO kasanari_iceberg_tables(catalog_name, namespace_name, table_name, metadata_location, previous_metadata_location)
            VALUES (?, ?, ?, ?, null)
            """;

    public static String UPDATE_TABLE = """
            UPDATE kasanari_iceberg_tables SET metadata_location = ?, previous_metadata_location = ?
            WHERE catalog_name = ? AND namespace_name = ? AND table_name = ? AND metadata_location = ?
            """;

    public static String CREATE_VIEW = """
            INSERT INTO kasanari_iceberg_views(catalog_name, namespace_name, view_name, metadata_location, previous_metadata_location)
            VALUES (?, ?, ?, ?, null)
            """;

    public static String UPDATE_VIEW = """
            UPDATE kasanari_iceberg_views SET metadata_location = ?, previous_metadata_location = ?
            WHERE catalog_name = ? AND namespace_name = ? AND view_name = ? AND metadata_location = ?
            """;

    public static String LIST_TABLES = """
            SELECT table_name FROM kasanari_iceberg_tables
            WHERE  catalog_name = ? AND namespace_name = ?
            ORDER BY table_name
            """;

    public static String LIST_VIEWS = """
            SELECT view_name FROM kasanari_iceberg_views
            WHERE  catalog_name = ? AND namespace_name = ?
            ORDER BY view_name
            """;

    public static String DELETE_TABLE = """
            DELETE FROM kasanari_iceberg_tables
            WHERE  catalog_name = ? AND namespace_name = ? AND table_name = ?
            """;

    public static String DELETE_VIEW = """
            DELETE FROM kasanari_iceberg_views
            WHERE  catalog_name = ? AND namespace_name = ? AND view_name = ?
            """;

    public static String RENAME_TABLE = """
            UPDATE kasanari_iceberg_tables SET namespace_name = ?, table_name = ?
            WHERE catalog_name = ? AND namespace_name = ? AND table_name = ?
            """;

    public static String RENAME_VIEW = """
            UPDATE kasanari_iceberg_views SET namespace_name = ?, view_name = ?
            WHERE catalog_name = ? AND namespace_name = ? AND view_name = ?
            """;
}
