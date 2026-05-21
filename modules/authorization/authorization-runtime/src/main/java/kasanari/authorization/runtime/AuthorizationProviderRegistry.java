package kasanari.authorization.runtime;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import kasanari.authorization.spi.AuthorizationProvider;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;

@Startup
@ApplicationScoped
public class AuthorizationProviderRegistry {
    private final AuthorizationProvider activeProvider;

    @Inject
    public AuthorizationProviderRegistry(AuthorizationConfiguration authorizationConfiguration) {
        var selectedType = authorizationConfiguration.type().trim().toLowerCase(Locale.ROOT);
        var providers = loadProviders();
        this.activeProvider = providers.get(selectedType);
        if (activeProvider == null) {
            throw new IllegalStateException(
                    "Unknown kasanari.authorization.type '" + selectedType + "'. Registered providers: " + providers.keySet());
        }

        var configProperties = readConfigProperties();
        activeProvider.initialize(new ConfigAuthorizationProviderContext(selectedType, configProperties));
    }

    public AuthorizationProvider activeProvider() {
        return activeProvider;
    }

    private static Map<String, AuthorizationProvider> loadProviders() {
        var providers = new HashMap<String, AuthorizationProvider>();
        for (var provider : ServiceLoader.load(AuthorizationProvider.class)) {
            var type = provider.type().trim().toLowerCase(Locale.ROOT);
            if (providers.putIfAbsent(type, provider) != null) {
                throw new IllegalStateException("Duplicate authorization provider type registered: " + type);
            }
        }
        if (providers.isEmpty()) {
            throw new IllegalStateException("No authorization providers registered via ServiceLoader");
        }
        return Map.copyOf(providers);
    }

    private static Map<String, String> readConfigProperties() {
        var properties = new HashMap<String, String>();
        for (var propertyName : ConfigProvider.getConfig().getPropertyNames()) {
            if (propertyName.startsWith("kasanari.authorization.")) {
                ConfigProvider.getConfig().getOptionalValue(propertyName, String.class)
                        .ifPresent(value -> properties.put(propertyName, value));
            }
        }
        return properties;
    }
}
