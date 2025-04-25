package kasanari.catalog.iceberg.core.model;

import org.apache.iceberg.Schema;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record IcebergTable(IcebergNamespace.Name namespace, Name name) {
    public TableIdentifier toIceberg() {
        return TableIdentifier.of(Namespace.of(namespace.levels()), name.value);
    }

    public record Name(String value) {}

    public record Location(String value) {
    }

    public record Listing(
            List<IcebergTable> tables,
            Optional<String> nextPageToken
    ) {
        public record Filter(
                Optional<String> pageToken,
                Optional<Integer> pageSize
        ) {
        }
    }

    public record CreateRequest(
            IcebergNamespace.Name namespace,
            Name name,
            Location location,
            Schema schema,
            Map<String, String> properties
    ) {

    }
}
