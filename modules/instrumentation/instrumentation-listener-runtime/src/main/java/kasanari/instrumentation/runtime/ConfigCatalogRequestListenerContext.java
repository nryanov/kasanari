package kasanari.instrumentation.runtime;

import kasanari.instrumentation.spi.CatalogRequestListenerContext;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

final class ConfigCatalogRequestListenerContext implements CatalogRequestListenerContext {
    private final Map<String, String> properties;

    ConfigCatalogRequestListenerContext(String listenerType, Map<String, String> configProperties) {
        var prefix = "kasanari.instrumentation." + listenerType + ".";
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
                new IllegalStateException("Missing required instrumentation property: " + key));
    }

    private static String normalize(String key) {
        return key.toLowerCase(Locale.ROOT);
    }
}
