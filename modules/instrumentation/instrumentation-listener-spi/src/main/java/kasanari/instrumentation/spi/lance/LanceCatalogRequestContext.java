package kasanari.instrumentation.spi.lance;

import java.util.Map;

public record LanceCatalogRequestContext(
        String catalogName,
        LanceCatalogOperation operation,
        String subject,
        Map<String, String> attributes
) {
}
