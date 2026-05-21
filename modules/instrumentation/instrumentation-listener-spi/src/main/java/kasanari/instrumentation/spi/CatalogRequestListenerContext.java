package kasanari.instrumentation.spi;

import java.util.Optional;

public interface CatalogRequestListenerContext {
    Optional<String> getOptional(String key);

    String getRequired(String key);
}
