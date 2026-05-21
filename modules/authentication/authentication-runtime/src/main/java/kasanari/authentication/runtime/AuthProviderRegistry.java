package kasanari.authentication.runtime;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import kasanari.authentication.spi.AuthProvider;
import kasanari.authentication.spi.AuthProviderMetadata;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;

@Startup
@ApplicationScoped
public class AuthProviderRegistry {
    private final AuthProvider activeProvider;
    private final AuthProviderMetadata metadata;

    @Inject
    public AuthProviderRegistry(AuthConfiguration authConfiguration) {
        var selectedType = authConfiguration.type().trim().toLowerCase(Locale.ROOT);
        var providers = loadProviders();
        this.activeProvider = providers.get(selectedType);
        if (activeProvider == null) {
            throw new IllegalStateException(
                    "Unknown kasanari.auth.type '" + selectedType + "'. Registered providers: " + providers.keySet());
        }

        var configProperties = readConfigProperties();
        activeProvider.initialize(new ConfigAuthProviderContext(selectedType, configProperties));
        this.metadata = activeProvider.metadata();
    }

    public AuthProvider activeProvider() {
        return activeProvider;
    }

    public AuthProviderMetadata metadata() {
        return metadata;
    }

    private static Map<String, AuthProvider> loadProviders() {
        var providers = new HashMap<String, AuthProvider>();
        for (var provider : ServiceLoader.load(AuthProvider.class)) {
            var type = provider.type().trim().toLowerCase(Locale.ROOT);
            if (providers.putIfAbsent(type, provider) != null) {
                throw new IllegalStateException("Duplicate auth provider type registered: " + type);
            }
        }
        if (providers.isEmpty()) {
            throw new IllegalStateException("No auth providers registered via ServiceLoader");
        }
        return Map.copyOf(providers);
    }

    private static Map<String, String> readConfigProperties() {
        var properties = new HashMap<String, String>();
        for (var propertyName : ConfigProvider.getConfig().getPropertyNames()) {
            if (propertyName.startsWith("kasanari.authentication.")) {
                ConfigProvider.getConfig().getOptionalValue(propertyName, String.class)
                        .ifPresent(value -> properties.put(propertyName, value));
            }
        }
        return properties;
    }
}
