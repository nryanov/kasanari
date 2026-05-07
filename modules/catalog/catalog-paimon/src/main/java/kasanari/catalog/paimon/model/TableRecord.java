package kasanari.catalog.paimon.model;

import org.apache.paimon.catalog.Identifier;

import java.util.Map;

public record TableRecord(
        String database,
        String name,
        Map<String, String> properties
) {
    public TableRecord(Identifier identifier, Map<String, String> properties) {
        this(identifier.getDatabaseName(), identifier.getTableName(), properties);
    }
}
