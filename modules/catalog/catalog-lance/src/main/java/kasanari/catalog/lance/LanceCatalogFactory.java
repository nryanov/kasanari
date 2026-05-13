package kasanari.catalog.lance;

import java.util.Map;

public interface LanceCatalogFactory {
    LanceCatalogAdapter create(String implementation, Map<String, String> properties);
}
