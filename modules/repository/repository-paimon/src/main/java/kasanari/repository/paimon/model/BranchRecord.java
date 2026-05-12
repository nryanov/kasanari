package kasanari.repository.paimon.model;

import java.util.Optional;

public record BranchRecord(
        String database,
        String table,
        String branchName,
        Optional<String> tagName
) {
}
