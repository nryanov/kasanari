package kasanari.catalog.iceberg.core.model;

import java.util.List;
import java.util.Optional;

public record IcebergNamespaceListing(
        List<IcebergNamespace.Name> namespaces,
        Optional<String> nextPageToken
) {
    public record Filter(
            Optional<String> parent,
            Optional<String> pageToken,
            Optional<Integer> pageSize
    ) {}
}
