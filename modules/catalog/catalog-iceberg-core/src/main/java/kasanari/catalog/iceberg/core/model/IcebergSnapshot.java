package kasanari.catalog.iceberg.core.model;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record IcebergSnapshot(
        Id id,
        Optional<Id> parentId,
        IcebergValues.SequenceNumber sequenceNumber,
        IcebergValues.Timestamp timestamp,
        IcebergValues.Location manifestList,
        Summary summary

) {
    public record Id(long value) {
    }

    public record Name(String value) {}

    public record Summary(
            Operation operation,
            Map<String, String> properties
    ) {
        public enum Operation {
            APPEND, REPLACE, OVERWRITE, DELETE
        }
    }

    public record Reference(
            Type type,
            Id id,
            KeepDuration maxRefAge,
            KeepDuration snapshotAge,
            KeepCount minSnapshotsToKeep

    ) {
        public enum Type {
            TAG, BRANCH
        }

        public record KeepDuration(Duration value) {
            public KeepDuration(long value) {
                this(Duration.ofMillis(value));
            }
        }

        public record KeepCount(int value) {}
    }

    public record Log(Id id, IcebergValues.Timestamp timestamp) {}

    public record Statistics(
            Id snapshotId,
            IcebergValues.Location path,
            IcebergValues.ByteSize fileSize,
            IcebergValues.ByteSize fileFooterSize,
            List<BlobMetadata> blobMetadata
    ) {
        public record BlobMetadata(
                Type type,
                Id snapshotId,
                IcebergValues.SequenceNumber sequenceNumber,
                List<Field> fields,
                Map<String, String> additionalProperties

        ) {
            public record Type(String value) {}

            public record Field(int value) {}
        }
    }

    public record PartitionStatistics(
            Id snapshotId,
            IcebergValues.Location path,
            IcebergValues.ByteSize fileSize
    ) {}
}
