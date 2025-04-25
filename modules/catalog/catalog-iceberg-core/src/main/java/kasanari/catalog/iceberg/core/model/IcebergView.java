package kasanari.catalog.iceberg.core.model;

import org.apache.iceberg.MetadataUpdate;
import org.apache.iceberg.Schema;
import org.apache.iceberg.UpdateRequirement;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.rest.requests.UpdateTableRequest;
import org.apache.iceberg.view.ImmutableSQLViewRepresentation;
import org.apache.iceberg.view.ImmutableViewVersion;
import org.apache.iceberg.view.ViewRepresentation;
import org.apache.iceberg.view.ViewVersion;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record IcebergView(IcebergNamespace.Name namespace, Name name) {
    public TableIdentifier toIceberg() {
        return TableIdentifier.of(Namespace.of(namespace.levels()), name.value);
    }

    public record Name(String value) {
    }

    public record Location(String value) {
    }

    public record CreateRequest(
            IcebergNamespace.Name namespace,
            Name name,
            Location location,
            Schema schema,
            Map<String, String> properties
    ) {
    }

    public record Uuid(String value) {
        public Uuid(UUID uuid) {
            this(uuid.toString());
        }
    }

    public record Metadata(
            Uuid uuid,
            FormatVersion formatVersion,
            Location location,
            VersionId currentVersionId,
            List<Version> versions,
            List<HistoryEntry> versionLog,
            List<Schema> schemas,
            Map<String, String> properties
    ) {

        public record FormatVersion(int value) {
        }

        public record VersionId(int value) {
        }

        public record SchemaId(int value) {
        }

        public record Timestamp(long value) {
        }

        public record Version(
                VersionId versionId,
                Timestamp timestampMs,
                SchemaId schemaId,
                Map<String, String> summary,
                List<Representation> representations,
                Optional<IcebergCatalog.Name> defaultCatalog,
                IcebergNamespace.Name namespace
        ) {
            public ViewVersion toIceberg() {
                return ImmutableViewVersion.builder()
                        .versionId(versionId.value)
                        .timestampMillis(timestampMs.value)
                        .schemaId(schemaId.value)
                        .summary(summary)
                        .defaultNamespace(Namespace.of(namespace.levels()))
                        .defaultCatalog(defaultCatalog.map(IcebergCatalog.Name::value).orElse(null))
                        .representations(representations.stream().map(Representation::toIceberg).toList())
                        .build();
            }

            public record Representation(Type type, Sql sql, Dialect dialect) {
                public ViewRepresentation toIceberg() {
                    return ImmutableSQLViewRepresentation
                            .builder()
                            .dialect(dialect.value)
                            .sql(sql.value)
                            .build();
                }

                public record Type(String value) {
                }

                public record Sql(String value) {
                }

                public record Dialect(String value) {
                }
            }
        }

        public record HistoryEntry(VersionId versionId, Timestamp timestampMs) {
        }
    }

    public record Listing(
            List<IcebergView> namespaces,
            Optional<String> nextPageToken
    ) {
        public record Filter(
                Optional<String> pageToken,
                Optional<Integer> pageSize
        ) {
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

        public sealed interface Requirement
                permits Requirement.AssertViewUUID {
            UpdateRequirement toIceberg();

            record AssertViewUUID(Uuid uuid) implements Requirement {
                @Override
                public UpdateRequirement toIceberg() {
                    return new UpdateRequirement.AssertViewUUID(uuid.value);
                }
            }
        }

        public sealed interface Update permits
                Update.AssignUUIDUpdate,
                Update.UpgradeFormatVersionUpdate,
                Update.AddSchemaUpdate,
                Update.SetLocationUpdate,
                Update.SetPropertiesUpdate,
                Update.RemovePropertiesUpdate,
                Update.AddViewVersionUpdate,
                Update.SetCurrentViewVersionUpdate {

            MetadataUpdate toIceberg();

            record AssignUUIDUpdate(Uuid uuid) implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.AssignUUID(uuid().value);
                }
            }

            record UpgradeFormatVersionUpdate(Metadata.FormatVersion version) implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.UpgradeFormatVersion(version.value);
                }
            }

            record AddSchemaUpdate(Schema schema, ColumnId lastColumnId) implements Update {
                public record ColumnId(int value) {
                }

                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.AddSchema(schema, lastColumnId.value);
                }
            }

            record SetLocationUpdate(Location location) implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.SetLocation(location.value);
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

            record AddViewVersionUpdate(Metadata.Version version) implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.AddViewVersion(version.toIceberg());
                }
            }

            record SetCurrentViewVersionUpdate(Metadata.VersionId currentVersion) implements Update {
                @Override
                public MetadataUpdate toIceberg() {
                    return new MetadataUpdate.SetCurrentViewVersion(currentVersion.value);
                }
            }
        }
    }
}
