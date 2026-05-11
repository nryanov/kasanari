package kasanari.catalog.paimon.model;

import org.apache.paimon.catalog.Identifier;

import java.util.Map;
import java.util.Optional;

public record TableRecord(
        String database,
        String name,
        Map<String, String> properties,
        Optional<String> tableUuid,
        long id
) {
    public TableRecord(
            String database,
            String name,
            Map<String, String> properties,
            Optional<String> tableUuid
    ) {
        this(database, name, properties, tableUuid, 0L);
    }

    public TableRecord(Identifier identifier, Map<String, String> properties) {
        this(identifier, properties, Optional.empty());
    }

    public TableRecord(Identifier identifier, Map<String, String> properties, Optional<String> tableUuid) {
        this(identifier.getDatabaseName(), identifier.getTableName(), properties, tableUuid);
    }
}
