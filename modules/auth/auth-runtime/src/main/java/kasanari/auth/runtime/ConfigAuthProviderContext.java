package kasanari.auth.runtime;

import kasanari.auth.spi.AuthProviderContext;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

final class ConfigAuthProviderContext implements AuthProviderContext {
    private final Map<String, String> properties;

    ConfigAuthProviderContext(String providerType, Map<String, String> configProperties) {
        var prefix = "kasanari.auth." + providerType + ".";
        this.properties = new HashMap<>();
        for (var entry : configProperties.entrySet()) {
            var key = entry.getKey();
            if (key.startsWith(prefix)) {
                properties.put(key.substring(prefix.length()), entry.getValue());
            }
        }
    }

    @Override
    public Optional<String> getOptional(String key) {
        return Optional.ofNullable(properties.get(normalize(key))).filter(value -> !value.isBlank());
    }

    @Override
    public String getRequired(String key) {
        return getOptional(key).orElseThrow(() ->
                new IllegalStateException("Missing required auth property: " + key));
    }

    @Override
    public Map<String, String> getProperties() {
        return Map.copyOf(properties);
    }

    private static String normalize(String key) {
        return key.toLowerCase(Locale.ROOT);
    }
}
