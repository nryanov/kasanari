package kasanari.catalog.iceberg.kasanari.repository.model;

public record IcebergTableRecord(
        String catalogName,
        String namespaceName,
        String tableNAme,
        String metadataLocation,
        String previousMetadataLocation
) {
}
