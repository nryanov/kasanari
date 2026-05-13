package kasanari.repository.lance.model;

import java.util.Map;

public record TableRow(
        String tableId,
        String namespacePath,
        String tableName,
        String location,
        Map<String, String> properties,
        boolean declaredOnly
) {
}
