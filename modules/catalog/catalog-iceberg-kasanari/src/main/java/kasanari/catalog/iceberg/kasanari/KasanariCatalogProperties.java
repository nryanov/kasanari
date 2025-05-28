package kasanari.catalog.iceberg.kasanari;

import java.time.Duration;

public final class KasanariCatalogProperties {
    private KasanariCatalogProperties() {}

    public static String WAREHOUSE = "warehouse";
    public static String URI = "uri";
    public static String USER = "kasanari.jdbc.user";
    public static String PASSWORD = "kasanari.jdbc.password";

    public static String STORAGE_TYPE = "kasanari.storage-type";
    public static KasanariStorageType STORAGE_TYPE_DEFAULT = KasanariStorageType.JDBC;

    public static String CONNECTION_POOL_INITIAL_SIZE = "kasanari.jdbc.connection-pool.initial-size";
    public static int CONNECTION_POOL_INITIAL_SIZE_DEFAULT = 2;

    public static String CONNECTION_POOL_MIN_SIZE = "kasanari.jdbc.connection-pool.min-size";
    public static int CONNECTION_POOL_MIN_SIZE_DEFAULT = 1;

    public static String CONNECTION_POOL_MAX_SIZE = "kasanari.jdbc.connection-pool.max-size";
    public static int CONNECTION_POOL_MAX_SIZE_DEFAULT = 5;

    public static String CONNECTION_MAX_LIFETIME_MILLIS = "kasanari.jdbc.connection-pool.max-lifetime.millis";
    public static Duration CONNECTION_MAX_LIFETIME_MILLIS_DEFAULT = Duration.ZERO; // eternal
}
