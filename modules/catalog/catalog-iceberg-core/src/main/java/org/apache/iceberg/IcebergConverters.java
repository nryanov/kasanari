package org.apache.iceberg;

import kasanari.catalog.iceberg.core.model.IcebergSnapshot;

public class IcebergConverters {
    public static SnapshotRefType toIceberg(IcebergSnapshot.Reference.Type type) {
        return SnapshotRefType.fromString(type.toString());
    }
}
