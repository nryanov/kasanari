package kasanari.catalog.management.model;


import java.util.ArrayList;
import java.util.List;

public class CatalogPublicInfo {
    private String catalogId;
    private CatalogType catalogType;
    private CatalogMode mode;
    private CatalogPublicSpec spec;
    private List<String> secretKeys = new ArrayList<>();
    private Long version;

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

    public CatalogPublicSpec getSpec() {
        return spec;
    }

    public void setSpec(CatalogPublicSpec spec) {
        this.spec = spec;
    }

    public List<String> getSecretKeys() {
        return secretKeys;
    }

    public void setSecretKeys(List<String> secretKeys) {
        this.secretKeys = secretKeys;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
