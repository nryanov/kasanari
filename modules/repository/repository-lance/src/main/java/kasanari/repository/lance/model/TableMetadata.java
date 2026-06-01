package kasanari.repository.lance.model;

import java.util.Map;

public record TableMetadata(
        String tableId,
        String namespacePath,
        String tableName,
        String location,
        Map<String, String> properties
) {
}
