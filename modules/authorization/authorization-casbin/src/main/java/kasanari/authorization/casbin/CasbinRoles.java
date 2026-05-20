package kasanari.authorization.casbin;

public final class CasbinRoles {
    public static final String ICEBERG_CATALOG_ADMIN = "IcebergCatalogAdmin";
    public static final String ICEBERG_CATALOG_EDITOR = "IcebergCatalogEditor";
    public static final String ICEBERG_CATALOG_VIEWER = "IcebergCatalogViewer";

    public static final String PAIMON_CATALOG_ADMIN = "PaimonCatalogAdmin";
    public static final String PAIMON_CATALOG_EDITOR = "PaimonCatalogEditor";
    public static final String PAIMON_CATALOG_VIEWER = "PaimonCatalogViewer";

    public static final String LANCE_CATALOG_ADMIN = "LanceCatalogAdmin";
    public static final String LANCE_CATALOG_EDITOR = "LanceCatalogEditor";
    public static final String LANCE_CATALOG_VIEWER = "LanceCatalogViewer";

    private CasbinRoles() {
    }
}
