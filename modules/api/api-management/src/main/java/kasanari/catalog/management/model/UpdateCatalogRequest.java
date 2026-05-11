package kasanari.catalog.management.model;

public class UpdateCatalogRequest {
    private CatalogSpec spec;
    private Long expectedVersion;

    public CatalogSpec getSpec() {
        return spec;
    }

    public void setSpec(CatalogSpec spec) {
        this.spec = spec;
    }

    public Long getExpectedVersion() {
        return expectedVersion;
    }

    public void setExpectedVersion(Long expectedVersion) {
        this.expectedVersion = expectedVersion;
    }
}
