package kasanari.catalog.iceberg.core.model;

public record IcebergCatalog(Name name) {
    public record Name(String value) {}
}
