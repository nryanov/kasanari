package kasanari.instrumentation.spi.paimon;

import java.util.Map;

public record PaimonCatalogRequestContext(
        String catalogName,
        PaimonCatalogOperation operation,
        String subject,
        Map<String, String> attributes
) {
}
