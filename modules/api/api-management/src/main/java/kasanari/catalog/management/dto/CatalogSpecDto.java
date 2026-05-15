package kasanari.catalog.management.dto;


public class CatalogSpecDto {
    private CatalogTypeDto type;
    private IcebergCatalogSpecModeConfigDto modeConfig;

    public CatalogTypeDto getType() {
        return type;
    }

    public void setType(CatalogTypeDto type) {
        this.type = type;
    }

    public IcebergCatalogSpecModeConfigDto getModeConfig() {
        return modeConfig;
    }

    public void setModeConfig(IcebergCatalogSpecModeConfigDto modeConfig) {
        this.modeConfig = modeConfig;
    }
}
