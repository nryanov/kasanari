package kasanari.catalog.iceberg.core.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record IcebergNamespace(
        Name name,
        Map<String, String> properties
) {
    public IcebergNamespace(String name) {
        this(new Name(name), Map.of());
    }

    public IcebergNamespace(List<String> levels, Map<String, String> properties) {
        this(new Name(levels.toArray(new String[0])), properties);
    }

    public IcebergNamespace(List<String> levels) {
        this(new Name(levels.toArray(new String[0])), Map.of());
    }

    public IcebergNamespace(String name, Map<String, String> properties) {
        this(new Name(name), properties);
    }

    public record Name(String[] levels) {
        public Name(String name) {
            this(name.split("[.]"));
        }
    }

    public record Listing(
            List<IcebergNamespace.Name> namespaces,
            Optional<String> nextPageToken
    ) {
        public record Filter(
                Optional<String> parent,
                Optional<String> pageToken,
                Optional<Integer> pageSize
        ) {}
    }
}
