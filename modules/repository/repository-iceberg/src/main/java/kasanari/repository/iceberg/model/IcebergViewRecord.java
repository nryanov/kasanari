package kasanari.repository.iceberg.model;

public record IcebergViewRecord(
        String catalogName,
        String namespaceName,
        String tableNAme,
        String metadataLocation,
        String previousMetadataLocation
) {
}
