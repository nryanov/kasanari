package kasanari.authorization.spi;

public enum Permission {
    // Iceberg tables
    IcebergTableList,
    IcebergTableCreate,
    IcebergTableGet,
    IcebergTableDrop,
    IcebergTableAlter,
    // Iceberg views
    IcebergViewList,
    IcebergViewCreate,
    IcebergViewGet,
    IcebergViewDrop,
    IcebergViewAlter,
    // Iceberg namespaces
    IcebergNamespaceList,
    IcebergNamespaceCreate,
    IcebergNamespaceGet,
    IcebergNamespaceDrop,
    IcebergNamespaceAlter,
    IcebergNamespaceExists,
    // Iceberg misc
    IcebergTransactionCommit,
    IcebergMetricsReport,
    IcebergTableExists,
    IcebergViewExists,
    // Iceberg management catalog metadata
    IcebergCatalogCreate,
    IcebergCatalogGet,
    IcebergCatalogUpdate,
    IcebergCatalogDelete,

    // Paimon databases
    PaimonDatabaseList,
    PaimonDatabaseCreate,
    PaimonDatabaseGet,
    PaimonDatabaseDrop,
    PaimonDatabaseAlter,
    // Paimon tables
    PaimonTableList,
    PaimonTableCreate,
    PaimonTableGet,
    PaimonTableDrop,
    PaimonTableAlter,
    PaimonTableExists,
    // Paimon views
    PaimonViewList,
    PaimonViewCreate,
    PaimonViewGet,
    PaimonViewDrop,
    PaimonViewAlter,
    // Paimon functions
    PaimonFunctionList,
    PaimonFunctionCreate,
    PaimonFunctionGet,
    PaimonFunctionDrop,
    PaimonFunctionAlter,
    // Paimon branches
    PaimonBranchList,
    PaimonBranchCreate,
    PaimonBranchDrop,
    PaimonBranchAlter,
    // Paimon partitions
    PaimonPartitionList,
    PaimonPartitionAlter,
    // Paimon tags
    PaimonTagList,
    PaimonTagCreate,
    PaimonTagGet,
    PaimonTagDrop,
    // Paimon config & management catalog
    PaimonConfigGet,
    PaimonCatalogCreate,
    PaimonCatalogGet,
    PaimonCatalogUpdate,
    PaimonCatalogDelete,

    // Lance namespaces
    LanceNamespaceList,
    LanceNamespaceCreate,
    LanceNamespaceGet,
    LanceNamespaceDrop,
    LanceNamespaceAlter,
    LanceNamespaceExists,
    // Lance tables
    LanceTableList,
    LanceTableCreate,
    LanceTableGet,
    LanceTableDrop,
    LanceTableAlter,
    LanceTableExists,
    // Lance management catalog
    LanceCatalogCreate,
    LanceCatalogGet,
    LanceCatalogUpdate,
    LanceCatalogDelete,

    // Role administration
    RoleSelect,
    RoleAdd,
    RoleRemove;

    public String wireName() {
        return name();
    }

    public static Permission fromName(String name) {
        return valueOf(name);
    }

    public static Permission catalogCreate(kasanari.repository.management.common.model.CatalogType type) {
        return switch (type) {
            case ICEBERG -> IcebergCatalogCreate;
            case PAIMON -> PaimonCatalogCreate;
            case LANCE -> LanceCatalogCreate;
        };
    }

    public static Permission catalogGet(kasanari.repository.management.common.model.CatalogType type) {
        return switch (type) {
            case ICEBERG -> IcebergCatalogGet;
            case PAIMON -> PaimonCatalogGet;
            case LANCE -> LanceCatalogGet;
        };
    }

    public static Permission catalogUpdate(kasanari.repository.management.common.model.CatalogType type) {
        return switch (type) {
            case ICEBERG -> IcebergCatalogUpdate;
            case PAIMON -> PaimonCatalogUpdate;
            case LANCE -> LanceCatalogUpdate;
        };
    }

    public static Permission catalogDelete(kasanari.repository.management.common.model.CatalogType type) {
        return switch (type) {
            case ICEBERG -> IcebergCatalogDelete;
            case PAIMON -> PaimonCatalogDelete;
            case LANCE -> LanceCatalogDelete;
        };
    }
}
