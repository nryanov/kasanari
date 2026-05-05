package kasanari.catalog.paimon.model;

import java.util.Map;

public record TableRecord(
        String database,
        String name,
        Map<String, String> properties
) {
}
