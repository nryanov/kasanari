package kasanari.catalog.management.model;

import java.util.HashMap;
import java.util.Map;

public class IcebergCatalogSpecModeConfig {
    private Map<String, String> properties = new HashMap<>();
    private Map<String, String> secrets = new HashMap<>();
    private String endpoint;

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getSecrets() {
        return secrets;
    }

    public void setSecrets(Map<String, String> secrets) {
        this.secrets = secrets;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
