package kasanari.catalog.paimon;

import java.util.Map;

public interface PaimonCatalogFactory {
    PaimonCatalogAdapter create(Map<String, String> fileIoProperties, Map<String, String> properties);
}
