package kasanari.authorization.casbin;

import java.time.Duration;
import java.util.Locale;

final class CasbinRefreshInterval {
    private static final Duration DEFAULT = Duration.ofMinutes(5);

    private CasbinRefreshInterval() {
    }

    static Duration parse(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT;
        }

        var trimmed = value.trim().toLowerCase(Locale.ROOT);
        if (trimmed.endsWith("ms")) {
            return Duration.ofMillis(Long.parseLong(trimmed.substring(0, trimmed.length() - 2)));
        }
        if (trimmed.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(trimmed.substring(0, trimmed.length() - 1)));
        }
        if (trimmed.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(trimmed.substring(0, trimmed.length() - 1)));
        }
        if (trimmed.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(trimmed.substring(0, trimmed.length() - 1)));
        }
        if (trimmed.endsWith("d")) {
            return Duration.ofDays(Long.parseLong(trimmed.substring(0, trimmed.length() - 1)));
        }

        return Duration.parse(trimmed);
    }
}
