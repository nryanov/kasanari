package kasanari.catalog.management.dto;

public class CreateCatalogRequestDto {
    private String catalogId;
    private CatalogTypeDto catalogType;
    private CatalogModeDto mode;
    private CatalogSpecDto spec;

    public String getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    public CatalogTypeDto getCatalogType() {
        return catalogType;
    }

    public void setCatalogType(CatalogTypeDto catalogType) {
        this.catalogType = catalogType;
    }

    public CatalogModeDto getMode() {
        return mode;
    }

    public void setMode(CatalogModeDto mode) {
        this.mode = mode;
    }

    public CatalogSpecDto getSpec() {
        return spec;
    }

    public void setSpec(CatalogSpecDto spec) {
        this.spec = spec;
    }
}
