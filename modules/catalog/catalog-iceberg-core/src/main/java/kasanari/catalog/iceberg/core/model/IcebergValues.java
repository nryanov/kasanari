package kasanari.catalog.iceberg.core.model;

import org.apache.iceberg.SchemaParser;

import java.util.UUID;

public final class IcebergValues {
    private IcebergValues() {}

    public record Location(String value) {
    }

    public record Uuid(
            String value
    ) {
        public Uuid(UUID uuid) {
            this(uuid.toString());
        }
    }

    public record FormatVersion(int value) {
    }

    public record VersionId(int value) {
    }

    public record SchemaId(int value) {
    }

    public record Timestamp(long value) {
    }

    public record ColumnId(int value) {
    }

    public record SequenceNumber(long value) {}

    public record ByteSize(long value) {}

    public record SourceId(int value) {
    }

    public record Schema(org.apache.iceberg.Schema value) {
        public Schema(String json) {
            this(SchemaParser.fromJson(json));
        }
    }
}
