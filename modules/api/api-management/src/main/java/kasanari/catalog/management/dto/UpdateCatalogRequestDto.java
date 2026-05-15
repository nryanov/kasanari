package kasanari.catalog.management.dto;

public class UpdateCatalogRequestDto {
    private CatalogSpecDto spec;
    private Long expectedVersion;

    public CatalogSpecDto getSpec() {
        return spec;
    }

    public void setSpec(CatalogSpecDto spec) {
        this.spec = spec;
    }

    public Long getExpectedVersion() {
        return expectedVersion;
    }

    public void setExpectedVersion(Long expectedVersion) {
        this.expectedVersion = expectedVersion;
    }
}
