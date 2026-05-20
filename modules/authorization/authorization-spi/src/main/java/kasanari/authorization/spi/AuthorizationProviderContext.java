package kasanari.authorization.spi;

import java.util.Optional;

public interface AuthorizationProviderContext {
    Optional<String> getOptional(String key);

    String getRequired(String key);
}
