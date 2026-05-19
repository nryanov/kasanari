package kasanari.auth.spi;

import java.util.Optional;

public interface AuthProviderContext {
    Optional<String> getOptional(String key);

    String getRequired(String key);
}
