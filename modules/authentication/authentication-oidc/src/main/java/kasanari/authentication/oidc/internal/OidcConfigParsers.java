package kasanari.authentication.oidc.internal;

import kasanari.authentication.spi.AuthProviderContext;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;

final class OidcConfigParsers {
    private OidcConfigParsers() {
    }

    static Optional<String> optionalString(AuthProviderContext context, String key) {
        return context.getOptional(key);
    }

    static String stringOrDefault(AuthProviderContext context, String key, String defaultValue) {
        return context.getOptional(key).orElse(defaultValue);
    }

    static boolean booleanOrDefault(AuthProviderContext context, String key, boolean defaultValue) {
        return context.getOptional(key).map(value -> parseBoolean(key, value)).orElse(defaultValue);
    }

    static Optional<Boolean> optionalBoolean(AuthProviderContext context, String key) {
        return context.getOptional(key).map(value -> parseBoolean(key, value));
    }

    static int intOrDefault(AuthProviderContext context, String key, int defaultValue) {
        return context.getOptional(key).map(value -> parseInt(key, value)).orElse(defaultValue);
    }

    static OptionalInt optionalInt(AuthProviderContext context, String key) {
        return context.getOptional(key)
                .map(value -> OptionalInt.of(parseInt(key, value)))
                .orElseGet(OptionalInt::empty);
    }

    static Duration durationOrDefault(AuthProviderContext context, String key, Duration defaultValue) {
        return context.getOptional(key).map(value -> parseDuration(key, value)).orElse(defaultValue);
    }

    static Optional<Duration> optionalDuration(AuthProviderContext context, String key) {
        return context.getOptional(key).map(value -> parseDuration(key, value));
    }

    static Optional<Path> optionalPath(AuthProviderContext context, String key) {
        return context.getOptional(key).map(value -> Path.of(value));
    }

    static <T extends Enum<T>> Optional<T> optionalEnum(
            AuthProviderContext context,
            String key,
            Class<T> type
    ) {
        return context.getOptional(key).map(value -> parseEnum(key, value, type));
    }

    private static boolean parseBoolean(String key, String raw) {
        var value = raw.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "true", "1", "yes", "on" -> true;
            case "false", "0", "no", "off" -> false;
            default -> throw invalid(key, raw, "boolean");
        };
    }

    private static int parseInt(String key, String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            throw invalid(key, raw, "integer");
        }
    }

    private static Duration parseDuration(String key, String raw) {
        var value = raw.trim();
        try {
            var lower = value.toLowerCase(Locale.ROOT);

            if (lower.endsWith("ms")) {
                return Duration.ofMillis(Long.parseLong(lower.substring(0, lower.length() - 2)));
            }

            var parseLong = Long.parseLong(lower.substring(0, lower.length() - 1));

            if (lower.endsWith("s")) {
                return Duration.ofSeconds(parseLong);
            }
            if (lower.endsWith("m")) {
                return Duration.ofMinutes(parseLong);
            }
            if (lower.endsWith("h")) {
                return Duration.ofHours(parseLong);
            }
            if (lower.endsWith("d")) {
                return Duration.ofDays(parseLong);
            }
            if (value.regionMatches(true, 0, "P", 0, 1)) {
                return Duration.parse(value);
            }
            return Duration.parse("PT" + value.toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw invalid(key, raw, "duration");
        }
    }

    private static <T extends Enum<T>> T parseEnum(String key, String raw, Class<T> type) {
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw invalid(key, raw, "one of " + java.util.Arrays.toString(type.getEnumConstants()));
        }
    }

    private static IllegalArgumentException invalid(String key, String raw, String expected) {
        return new IllegalArgumentException(
                "Invalid value for auth property '" + key + "': '" + raw + "' (expected " + expected + ")");
    }
}
