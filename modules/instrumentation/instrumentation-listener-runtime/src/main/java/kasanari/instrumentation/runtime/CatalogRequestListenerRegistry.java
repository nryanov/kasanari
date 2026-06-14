package kasanari.instrumentation.runtime;

import kasanari.instrumentation.spi.CatalogRequestListener;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

public final class CatalogRequestListenerRegistry {
    private CatalogRequestListenerRegistry() {
    }

    public static List<CatalogRequestListener> assembleListeners(
            InstrumentationConfiguration configuration,
            List<CatalogRequestListener> internalListeners
    ) {
        var enabledTypes = parseEnabledTypes(configuration.listeners());
        // add internal CDI listeners
        internalListeners.forEach(it -> enabledTypes.add(it.type()));

        var configProperties = readConfigProperties();
        var listeners = new ArrayList<CatalogRequestListener>();

        // SPI listeners
        for (var listener : ServiceLoader.load(CatalogRequestListener.class)) {
            registerListener(listener, enabledTypes, configProperties, listeners);
        }

        // internal CDI listeners
        for (var listener : internalListeners) {
            registerListener(listener, enabledTypes, configProperties, listeners);
        }

        return listeners;
    }

    private static void registerListener(
            CatalogRequestListener listener,
            Set<String> enabledTypes,
            Map<String, String> configProperties,
            List<CatalogRequestListener> listeners
    ) {
        var type = listener.type().trim().toLowerCase(Locale.ROOT);

        if (!enabledTypes.contains(type)) {
            return;
        }

        if (listeners.stream().anyMatch(existing -> existing.type().equalsIgnoreCase(type))) {
            throw new IllegalStateException("Duplicate catalog request listener type registered: " + type);
        }

        listener.initialize(new ConfigCatalogRequestListenerContext(type, configProperties));
        listeners.add(listener);
    }

    private static Set<String> parseEnabledTypes(String listeners) {
        if (listeners == null || listeners.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(listeners.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private static Map<String, String> readConfigProperties() {
        var properties = new HashMap<String, String>();
        for (var propertyName : ConfigProvider.getConfig().getPropertyNames()) {
            if (propertyName.startsWith("kasanari.instrumentation.")) {
                ConfigProvider.getConfig().getOptionalValue(propertyName, String.class)
                        .ifPresent(value -> properties.put(propertyName, value));
            }
        }
        return properties;
    }
}
