package kasanari.catalog.paimon;

import java.util.Map;

public interface PaimonCatalogFactory {
    PaimonCatalogAdapter create(String name, Map<String, String> fileIoProperties, Map<String, String> properties);
}
