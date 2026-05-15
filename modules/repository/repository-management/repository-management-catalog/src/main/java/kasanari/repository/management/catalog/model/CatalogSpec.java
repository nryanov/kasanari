package kasanari.repository.management.catalog.model;

import java.util.Map;

public record CatalogSpec(
        Map<String, String> fileIoProperties,
        Map<String, String> catalogProperties,
        String endpoint
) {
}
