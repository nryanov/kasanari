package org.apache.iceberg;

import kasanari.catalog.iceberg.core.model.IcebergSnapshot;
import kasanari.catalog.iceberg.core.model.IcebergValues;

public class IcebergConverters {
    public static SnapshotRefType toIceberg(IcebergSnapshot.Reference.Type type) {
        return SnapshotRefType.fromString(type.toString());
    }

    public static org.apache.iceberg.Snapshot toIceberg(IcebergValues.SchemaId id, IcebergSnapshot snapshot) {
        return new BaseSnapshot(
                snapshot.sequenceNumber().value(),
                snapshot.id().value(),
                snapshot.parentId().map(IcebergSnapshot.Id::value).orElse(null),
                snapshot.timestamp().value(),
                snapshot.summary().operation().name(),
                snapshot.summary().properties(),
                id.value(),
                snapshot.manifestList().value()
        );
    }
}
