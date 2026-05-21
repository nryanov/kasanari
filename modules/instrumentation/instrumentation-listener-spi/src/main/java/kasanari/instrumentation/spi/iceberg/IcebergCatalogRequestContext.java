package kasanari.instrumentation.spi.iceberg;

import java.util.Map;

public record IcebergCatalogRequestContext(
        String catalogName,
        IcebergCatalogOperation operation,
        String subject,
        Map<String, String> attributes
) {
}
