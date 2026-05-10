package kasanari.catalog.paimon.model;

import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.view.View;

import java.util.Map;
import java.util.Optional;

public record ViewRecord(String database,
                         String name,
                         String query,
                         Map<String, String> dialects,
                         Map<String, String> options,
                         Optional<String> comment,
                         long id
) {
    public ViewRecord(
            String database,
            String name,
            String query,
            Map<String, String> dialects,
            Map<String, String> options,
            Optional<String> comment
    ) {
        this(database, name, query, dialects, options, comment, 0L);
    }

    public ViewRecord(Identifier identifier, View view) {
        this(
                identifier.getDatabaseName(),
                identifier.getTableName(),
                view.query(),
                view.dialects(),
                view.options(),
                view.comment(),
                0L
        );
    }
}
