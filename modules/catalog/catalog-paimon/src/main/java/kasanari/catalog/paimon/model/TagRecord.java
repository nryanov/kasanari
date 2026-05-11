package kasanari.catalog.paimon.model;

import java.util.Optional;

public record TagRecord(
        String database,
        String table,
        String tagName,
        long snapshotId,
        Optional<Long> tagCreateTime,
        Optional<String> tagTimeRetained,
        long id
) {
    public TagRecord(
            String database,
            String table,
            String tagName,
            long snapshotId,
            Optional<Long> tagCreateTime,
            Optional<String> tagTimeRetained
    ) {
        this(database, table, tagName, snapshotId, tagCreateTime, tagTimeRetained, 0L);
    }
}
