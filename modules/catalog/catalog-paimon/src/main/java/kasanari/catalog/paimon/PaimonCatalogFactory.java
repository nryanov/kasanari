package kasanari.catalog.paimon;

import java.util.Map;

public interface PaimonCatalogFactory {
    /**
     * @param name management catalog id used for INTERNAL row isolation (ignored by PROXY)
     */
    PaimonCatalogAdapter create(String name, Map<String, String> fileIoProperties, Map<String, String> properties);
}
