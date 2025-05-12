package kasanari.catalog.iceberg.core.model;

import org.apache.iceberg.MetadataUpdate;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.SortDirection;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.UpdateRequirement;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.rest.requests.UpdateTableRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public record IcebergTable(IcebergNamespace.Name namespace, Name name) {
    public TableIdentifier toIceberg() {
        return TableIdentifier.of(Namespace.of(namespace.levels()), name.value);
    }

    public record Name(String value) {
    }

    public record Listing(
            List<IcebergTable> tables,
            Optional<String> nextPageToken
    ) {
        public record Filter(
                Optional<String> pageToken,
                Optional<Integer> pageSize
        ) {
            public final static int DEFAULT_PAGE_SIZE = 20;

            public Filter() {
                this(Optional.empty(), Optional.of(DEFAULT_PAGE_SIZE));
            }
        }
    }

    public record CreateRequest(
            IcebergNamespace.Name namespace,
            Name name,
            IcebergValues.Schema schema,
            IcebergValues.Location location,
            PartitionSpecification partitionSpecification,
            SortSpecification sortSpecification,
            Map<String, String> properties
    ) { }

    public sealed interface SortSpecification permits SortSpecification.Sorted, SortSpecification.Unsorted {
        record Id(int value) {
        }

        SortOrder toIceberg(IcebergValues.Schema schema);

        record Unsorted() implements SortSpecification {
            @Override
            public SortOrder toIceberg(IcebergValues.Schema schema) {
                return SortOrder.unsorted();
            }
        }

        record Sorted(
                Id id,
                List<Field> fields
        ) implements SortSpecification {
            @Override
            public SortOrder toIceberg(IcebergValues.Schema schema) {
                var builder = SortOrder
                        .builderFor(schema.value())
                        .withOrderId(id.value);

                fields.forEach(field -> {
                    var nestedField = schema.value().findField(field.sourceId.value());
                    builder.sortBy(nestedField.name(), field.direction.icebergDirection, field.nullOrder.icebergNullOrder);
                });

                return builder.build();
            }

            public record Field(
                    IcebergValues.SourceId sourceId,
                    Transform transform,
                    Direction direction,
                    NullOrder nullOrder

            ) {
            }

            public enum Direction {
                ASC(SortDirection.ASC),
                DESC(SortDirection.DESC);

                public final SortDirection icebergDirection;

                Direction(SortDirection icebergDirection) {
                    this.icebergDirection = icebergDirection;
                }
            }

            public enum NullOrder {
                NULLS_FIRST(org.apache.iceberg.NullOrder.NULLS_FIRST),
                NULLS_LAST(org.apache.iceberg.NullOrder.NULLS_LAST);

                public final org.apache.iceberg.NullOrder icebergNullOrder;

                NullOrder(org.apache.iceberg.NullOrder icebergNullOrder) {
                    this.icebergNullOrder = icebergNullOrder;
                }
            }
        }
    }

    public sealed interface PartitionSpecification permits PartitionSpecification.Partitioned, PartitionSpecification.Unpartitioned {
        record Id(int value) {}

        PartitionSpec toIceberg(IcebergValues.Schema schema);

        record Unpartitioned() implements PartitionSpecification {
            @Override
            public PartitionSpec toIceberg(IcebergValues.Schema schema) {
                return PartitionSpec.unpartitioned();
            }
        }

        record Partitioned(
                Optional<Id> specId,
                List<Field> fields
        ) implements PartitionSpecification {
            @Override
            public PartitionSpec toIceberg(IcebergValues.Schema schema) {
                var builder = PartitionSpec.builderFor(schema.value());

                specId.ifPresent(it -> builder.withSpecId(it.value));
                fields.forEach(field -> {
                    switch (field.transform) {
                        case Transform.Identity _ -> builder.identity(field.name.value);
                        case Transform.Year _ -> builder.year(field.name.value);
                        case Transform.Month _ -> builder.month(field.name.value);
                        case Transform.Day _ -> builder.day(field.name.value);
                        case Transform.Hour _ -> builder.hour(field.name.value);
                        case Transform.Bucket bucket -> builder.bucket(field.name.value, bucket.buckets);
                        case Transform.Truncate truncate -> builder.truncate(field.name.value, truncate.length);
                    }
                });

                return builder.build();
            }

            public record Field(
                    Optional<IcebergValues.ColumnId> fieldId,
                    IcebergValues.SourceId sourceId,
                    Name name,
                    Transform transform
            ) {
                public record Name(String value) {
                }
            }
        }
    }

    public sealed interface Transform permits
            Transform.Identity,
            Transform.Year,
            Transform.Month,
            Transform.Day,
            Transform.Hour,
            Transform.Bucket,
            Transform.Truncate {
        Pattern BUCKET = Pattern.compile("bucket\\[(\\d.)]");
        Pattern TRUNCATE = Pattern.compile("truncate\\[(\\d.)]");

        static Transform fromIceberg(org.apache.iceberg.transforms.Transform<?, ?> transform) {
            var literal = transform.toString();
            return switch (literal) {
                case "identity" -> new Transform.Identity();
                case "year" -> new Transform.Year();
                case "month" -> new Transform.Month();
                case "day" -> new Transform.Day();
                case "hour" -> new Transform.Hour();
                default -> {
                    var maybeBucket = BUCKET.matcher(literal);
                    var maybeTruncate = TRUNCATE.matcher(literal);

                    if (maybeBucket.find()) {
                        yield new Transform.Bucket(Integer.parseInt(maybeBucket.group(1)));
                    }

                    if (maybeTruncate.find()) {
                        yield new Transform.Truncate(Integer.parseInt(maybeTruncate.group(1)));
                    }

                    throw new IllegalArgumentException("Unknown partition type");
                }
            };
        }

        record Identity() implements Transform {
        }

        record Year() implements Transform {
        }

        record Month() implements Transform {
        }

        record Day() implements Transform {
        }

        record Hour() implements Transform {
        }

        record Bucket(int buckets) implements Transform {
        }

        record Truncate(int length) implements Transform {
        }
    }

    public record UpdateRequest(
            List<Requirement> requirements,
            List<Update> updates
    ) {
        public UpdateTableRequest toIceberg(TableIdentifier identifier) {
            return UpdateTableRequest.create(
                    identifier,
                    requirements.stream().map(Requirement::toIceberg).toList(),
                    updates.stream().map(Update::toIceberg).toList()
            );
        }

        // https://github.com/apache/iceberg/blob/main/open-api/rest-catalog-open-api.yaml#L3010
        public sealed interface Update permits
                Update.AssignUuidUpdate,
                Update.UpgradeFormatVersionUpdate,
                Update.AddSchemaUpdate,
                Update.SetCurrentSchemaUpdate,
                Update.AddPartitionSpecUpdate,
                Update.SetDefaultSpecUpdate,
                Update.AddSortOrderUpdate,
                Update.SetDefaultSortOrderUpdate,
                Update.AddSnapshotUpdate,
                Update.SetSnapshotRefUpdate,
                Update.RemoveSnapshotsUpdate,
                Update.RemoveSnapshotRefUpdate,
                Update.SetLocationUpdate,
                Update.SetPropertiesUpdate,
                Update.RemovePropertiesUpdate,
                Update.SetStatisticsUpdate,
                Update.RemoveStatisticsUpdate,
                Update.RemovePartitionSpecsUpdate,
                Update.EnableRowLineageUpdate {
            MetadataUpdate toIceberg();

            record AssignUuidUpdate(IcebergValues.Uuid uuid) implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.AssignUUID(uuid.value());
                }
            }

            record UpgradeFormatVersionUpdate(IcebergValues.FormatVersion formatVersion) implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.UpgradeFormatVersion(formatVersion.value());
                }
            }

            record AddSchemaUpdate(IcebergValues.Schema schema, IcebergValues.ColumnId lastColumnId) implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.AddSchema(schema.value(), lastColumnId.value());
                }
            }

            record SetCurrentSchemaUpdate(IcebergValues.SchemaId schemaId) implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.SetCurrentSchema(schemaId.value());
                }
            }

            record AddPartitionSpecUpdate(IcebergValues.Schema schema, PartitionSpecification partitionSpecification) implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.AddPartitionSpec(partitionSpecification.toIceberg(schema));
                }
            }

            record SetDefaultSpecUpdate(PartitionSpecification.Id specId) implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.SetDefaultPartitionSpec(specId.value());
                }
            }

            record AddSortOrderUpdate(IcebergValues.Schema schema, SortSpecification sortSpecification) implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.AddSortOrder(sortSpecification.toIceberg(schema));
                }
            }

            record SetDefaultSortOrderUpdate(SortSpecification.Id orderId) implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.SetDefaultSortOrder(orderId.value);
                }
            }

            record AddSnapshotUpdate() implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    throw new UnsupportedOperationException("AddSnapshotUpdate");
                }
            }

            record SetSnapshotRefUpdate() implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    throw new UnsupportedOperationException("SetSnapshotRefUpdate");
                }
            }

            record RemoveSnapshotsUpdate() implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    throw new UnsupportedOperationException("RemoveSnapshotsUpdate");
                }
            }

            record RemoveSnapshotRefUpdate() implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return null;
                }
            }

            record SetLocationUpdate(IcebergValues.Location location) implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.SetLocation(location.value());
                }
            }

            record SetPropertiesUpdate(Map<String, String> properties) implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.SetProperties(properties);
                }
            }

            record RemovePropertiesUpdate(Set<String> removed) implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.RemoveProperties(removed);
                }
            }

            record SetStatisticsUpdate() implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    throw new UnsupportedOperationException("SetStatisticsUpdate");
                }
            }

            record RemoveStatisticsUpdate(IcebergSnapshot.Id id) implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.RemoveStatistics(id.value());
                }
            }

            record RemovePartitionSpecsUpdate() implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    // todo
                    return new MetadataUpdate.AddPartitionSpec(PartitionSpec.unpartitioned());
                }
            }

            record EnableRowLineageUpdate() implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    throw new UnsupportedOperationException("EnableRowLineageUpdate");
                }
            }
        }

        // https://github.com/apache/iceberg/blob/main/open-api/rest-catalog-open-api.yaml#L3044
        public sealed interface Requirement permits
                Requirement.AssertCreate,
                Requirement.AssertTableUuid,
                Requirement.AssertRefSnapshotId,
                Requirement.AssertLastAssignedFieldId,
                Requirement.AssertCurrentSchemaId,
                Requirement.AssertLastAssignedPartitionId,
                Requirement.AssertDefaultSpecId,
                Requirement.AssertDefaultSortOrderId {
            UpdateRequirement toIceberg();

            record AssertCreate() implements Requirement {
                @Override
                public UpdateRequirement toIceberg() {
                    return new UpdateRequirement.AssertTableDoesNotExist();
                }
            }

            record AssertTableUuid(IcebergValues.Uuid uuid) implements Requirement {
                @Override
                public UpdateRequirement toIceberg() {
                    return new UpdateRequirement.AssertTableUUID(uuid.value());
                }
            }

            record AssertRefSnapshotId(IcebergSnapshot.Name name, IcebergSnapshot.Id id) implements Requirement {
                @Override
                public UpdateRequirement toIceberg() {
                    return new UpdateRequirement.AssertRefSnapshotID(name.value(), id.value());
                }
            }

            record AssertLastAssignedFieldId(IcebergValues.ColumnId id) implements Requirement {
                @Override
                public UpdateRequirement toIceberg() {
                    return new UpdateRequirement.AssertLastAssignedFieldId(id.value());
                }
            }

            record AssertCurrentSchemaId(IcebergValues.SchemaId id) implements Requirement {
                @Override
                public UpdateRequirement toIceberg() {
                    return new UpdateRequirement.AssertCurrentSchemaID(id.value());
                }
            }

            record AssertLastAssignedPartitionId(PartitionSpecification.Id specId) implements Requirement {
                @Override
                public UpdateRequirement toIceberg() {
                    return new UpdateRequirement.AssertLastAssignedPartitionId(specId.value);
                }
            }

            record AssertDefaultSpecId(PartitionSpecification.Id specId) implements Requirement {
                @Override
                public UpdateRequirement toIceberg() {
                    return new UpdateRequirement.AssertDefaultSpecID(specId.value);
                }
            }

            record AssertDefaultSortOrderId(SortSpecification.Id orderId) implements Requirement {
                @Override
                public UpdateRequirement toIceberg() {
                    return new UpdateRequirement.AssertDefaultSortOrderID(orderId.value);
                }
            }

        }
    }

    public record LoadedTable(
            Metadata metadata,
            Map<String, String> config,
            Optional<StorageCredential> credential
    ) {
        public LoadedTable(Metadata metadata) {
            this(metadata, Map.of(), Optional.empty());
        }

        public record StorageCredential(
                Prefix prefix,
                Map<String, String> config
        ) {
            public record Prefix(String value) {}
        }
    }

    public record Commit(
            IcebergValues.Location metadataLocation,
            Metadata metadata
    ) {

    }

    public record Metadata(
            IcebergValues.FormatVersion formatVersion,
            IcebergValues.Uuid uuid,
            IcebergValues.Location location,
            IcebergValues.Timestamp lastUpdated,
            Map<String, String> properties,
            List<IcebergValues.Schema> schemas,
            IcebergValues.SchemaId currentSchemaId,
            IcebergValues.ColumnId lastColumnId,
            List<PartitionSpecification> partitionSpecifications,
            PartitionSpecification.Id defaultSpecId,
            PartitionSpecification.Id lastPartitionId,
            List<SortSpecification> sortSpecifications,
            SortSpecification.Id defaultSortOderdId,
            List<IcebergSnapshot> snapshots,
            Map<String, IcebergSnapshot.Reference> snapshotRefs,
            Optional<IcebergSnapshot.Id> currentSnapshotId,
            IcebergValues.SequenceNumber lastSequenceNumber,
            List<IcebergSnapshot.Log> snapshotLogs,
            List<Log> metadataLog,
            List<IcebergSnapshot.Statistics> statistics,
            List<IcebergSnapshot.PartitionStatistics> partitionStatistics
    ) {
        public record Log(
                IcebergValues.Location file,
                IcebergValues.Timestamp timestamp
        ) {}
    }

    public record Transaction(
            IcebergTable table,
            UpdateRequest changes
    ) {}
}
