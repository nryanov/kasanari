package kasanari.repository.jdbc;

import java.time.Duration;

public abstract class KasanariDataSourceConfiguration {
    public static String URI = "uri";
    public static String USER = "kasanari.jdbc.user";
    public static String PASSWORD = "kasanari.jdbc.password";

    /** Repository implementation selector: {@code postgres} (default) or {@code yugabyte}. */
    public static String REPOSITORY_BACKEND = "kasanari.repository.backend";

    public static String CONNECTION_POOL_INITIAL_SIZE = "kasanari.jdbc.connection-pool.initial-size";
    public static int CONNECTION_POOL_INITIAL_SIZE_DEFAULT = 2;

    public static String CONNECTION_POOL_MIN_SIZE = "kasanari.jdbc.connection-pool.min-size";
    public static int CONNECTION_POOL_MIN_SIZE_DEFAULT = 1;

    public static String CONNECTION_POOL_MAX_SIZE = "kasanari.jdbc.connection-pool.max-size";
    public static int CONNECTION_POOL_MAX_SIZE_DEFAULT = 5;

    public static String CONNECTION_MAX_LIFETIME_MILLIS = "kasanari.jdbc.connection-pool.max-lifetime.millis";
    public static Duration CONNECTION_MAX_LIFETIME_MILLIS_DEFAULT = Duration.ZERO; // eternal
}
