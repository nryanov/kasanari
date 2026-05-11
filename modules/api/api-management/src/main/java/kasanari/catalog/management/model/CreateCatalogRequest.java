package kasanari.catalog.management.model;

public class CreateCatalogRequest {
    private String catalogId;
    private CatalogType catalogType;
    private CatalogMode mode;
    private CatalogSpec spec;

    public String getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(String catalogId) {
        this.catalogId = catalogId;
    }

    public CatalogType getCatalogType() {
        return catalogType;
    }

    public void setCatalogType(CatalogType catalogType) {
        this.catalogType = catalogType;
    }

    public CatalogMode getMode() {
        return mode;
    }

    public void setMode(CatalogMode mode) {
        this.mode = mode;
    }

    public CatalogSpec getSpec() {
        return spec;
    }

    public void setSpec(CatalogSpec spec) {
        this.spec = spec;
    }
}
