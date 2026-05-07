package kasanari.catalog.paimon.model;

import java.util.Optional;

public record TagRecord(
        String database,
        String table,
        String tagName,
        long snapshotId,
        Optional<Long> tagCreateTime,
        Optional<String> tagTimeRetained
) {
}
