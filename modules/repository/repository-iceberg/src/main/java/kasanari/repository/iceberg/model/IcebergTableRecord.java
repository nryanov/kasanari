package kasanari.repository.iceberg.model;

public record IcebergTableRecord(
        String catalogName,
        String namespaceName,
        String tableNAme,
        String metadataLocation,
        String previousMetadataLocation
) {
}
