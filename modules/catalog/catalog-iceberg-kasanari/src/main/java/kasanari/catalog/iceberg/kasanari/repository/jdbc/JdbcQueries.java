package kasanari.catalog.iceberg.kasanari.repository.jdbc;

public class JdbcQueries {
    private final static String CREATE_CATALOGS_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_iceberg_catalog
            (
                catalog_name TEXT,
                CONSTRAINT kasanari_iceberg_catalog PRIMARY KEY (catalog_name)
            )
            """;

    private final static String CREATE_NAMESPACES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_iceberg_namespaces
            (
                catalog_name   TEXT,
                namespace_name TEXT,
                CONSTRAINT kasanari_iceberg_namespaces PRIMARY KEY (catalog_name, namespace_name),
                CONSTRAINT kasanari_iceberg_namespaces_catalog_fk FOREIGN KEY (catalog_name) REFERENCES kasanari_iceberg_catalog (catalog_name)
            )
            """;

    private final static String CREATE_NAMESPACE_PROPERTIES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_iceberg_namespace_properties
            (
                catalog_name   TEXT,
                namespace_name TEXT,
                property_key   TEXT,
                property_value TEXT,
                CONSTRAINT kasanari_iceberg_namespace_properties_pk PRIMARY KEY (catalog_name, namespace_name, property_key),
                CONSTRAINT kasanari_iceberg_namespace_properties_catalog_fk FOREIGN KEY (catalog_name) REFERENCES kasanari_iceberg_catalog (catalog_name),
                CONSTRAINT kasanari_iceberg_namespace_properties_namespace_fk FOREIGN KEY (namespace_name) REFERENCES kasanari_iceberg_namespaces (namespace_name)
            )
            """;

    private final static String CREATE_TABLES_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_iceberg_tables
            (
                catalog_name               TEXT,
                namespace_name             TEXT,
                table_name                 TEXT,
                metadata_location          TEXT,
                previous_metadata_location TEXT,
                CONSTRAINT kasanari_iceberg_tables_pk PRIMARY KEY (catalog_name, namespace_name, table_name),
                CONSTRAINT kasanari_iceberg_tables_catalog_fk FOREIGN KEY (catalog_name) REFERENCES kasanari_iceberg_catalog (catalog_name),
                CONSTRAINT kasanari_iceberg_tables_namespace_fk FOREIGN KEY (namespace_name) REFERENCES kasanari_iceberg_namespaces (namespace_name)
            )
            """;

    private final static String CREATE_VIEWS_DDL = """
            CREATE TABLE IF NOT EXISTS kasanari_iceberg_views
            (
                catalog_name               TEXT,
                namespace_name             TEXT,
                view_name                  TEXT,
                metadata_location          TEXT,
                previous_metadata_location TEXT,
                CONSTRAINT kasanari_iceberg_views_pk PRIMARY KEY (catalog_name, namespace_name, view_name),
                CONSTRAINT kasanari_iceberg_views_catalog_fk FOREIGN KEY (catalog_name) REFERENCES kasanari_iceberg_catalog (catalog_name),
                CONSTRAINT kasanari_iceberg_views_namespace_fk FOREIGN KEY (namespace_name) REFERENCES kasanari_iceberg_namespaces (namespace_name)
            )
            """;
}
