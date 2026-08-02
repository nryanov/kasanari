package kasanari.repository.jdbc;

import java.util.Locale;
import java.util.Map;

/**
 * JDBC repository implementation selector.
 *
 * <p>Configured via {@link KasanariDataSourceConfiguration#REPOSITORY_BACKEND}.
 * Defaults to {@link #POSTGRES} when unset.
 */
public enum RepositoryBackend {
    POSTGRES,
    YUGABYTE;

    public static RepositoryBackend from(Map<String, String> properties) {
        if (properties == null) {
            return POSTGRES;
        }
        return parse(properties.get(KasanariDataSourceConfiguration.REPOSITORY_BACKEND));
    }

    public static RepositoryBackend parse(String value) {
        if (value == null || value.isBlank()) {
            return POSTGRES;
        }
        var normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "postgres", "postgresql" -> POSTGRES;
            case "yugabyte", "yugabytedb" -> YUGABYTE;
            default -> throw new IllegalArgumentException(
                    "Unsupported " + KasanariDataSourceConfiguration.REPOSITORY_BACKEND
                            + " value `" + value + "`. Expected `postgres` or `yugabyte`."
            );
        };
    }
}
