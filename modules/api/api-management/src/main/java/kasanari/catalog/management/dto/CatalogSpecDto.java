package kasanari.catalog.management.dto;


import java.util.HashMap;
import java.util.Map;

public class CatalogSpecDto {
    private Map<String, String> fileIoProperties = new HashMap<>();
    private Map<String, String> catalogProperties = new HashMap<>();

    public Map<String, String> getFileIoProperties() {
        return fileIoProperties;
    }

    public void setFileIoProperties(Map<String, String> fileIoProperties) {
        this.fileIoProperties = fileIoProperties;
    }

    public Map<String, String> getCatalogProperties() {
        return catalogProperties;
    }

    public void setCatalogProperties(Map<String, String> catalogProperties) {
        this.catalogProperties = catalogProperties;
    }
}
