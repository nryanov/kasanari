package kasanari.catalog.iceberg.core.model;

import org.apache.iceberg.catalog.Namespace;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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

        public Namespace toIceberg() {
            return Namespace.of(levels);
        }

        @Override
        public String toString() {
            return "Name{" +
                    "levels=" + Arrays.toString(levels) +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Name name = (Name) o;
            return Objects.deepEquals(levels, name.levels);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(levels);
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
        ) {
            public final static int DEFAULT_PAGE_SIZE = 20;


            public Filter() {
                this(Optional.empty(), Optional.empty(), Optional.of(DEFAULT_PAGE_SIZE));
            }
        }
    }

    public record Update(
            Set<String> removals,
            Map<String, String> updates
    ) {}
}
