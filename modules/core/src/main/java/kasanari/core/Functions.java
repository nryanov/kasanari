package kasanari.core;

import java.util.Map;

public abstract class Functions {
    public static Map<String, String> mapOrEmpty(Map<String, String> input) {
        if (input == null) {
            return Map.of();
        }

        return input;
    }

    public static <T> T valueOrDefault(T maybe, T defaultValue) {
        if (maybe == null) {
            return defaultValue;
        }

        return maybe;
    }
}
