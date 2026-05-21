package kasanari.authorization.runtime;

import kasanari.authorization.spi.AuthorizationProviderContext;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class ConfigAuthorizationProviderContext implements AuthorizationProviderContext {
    private final Map<String, String> properties;

    ConfigAuthorizationProviderContext(String providerType, Map<String, String> configProperties) {
        var prefix = "kasanari.authorization." + providerType + ".";
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
                new IllegalStateException("Missing required authorization property: " + key));
    }

    private static String normalize(String key) {
        return key.toLowerCase(Locale.ROOT);
    }
}
