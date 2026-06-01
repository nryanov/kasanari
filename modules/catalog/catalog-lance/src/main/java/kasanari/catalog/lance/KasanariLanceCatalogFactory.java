package kasanari.catalog.lance;

import org.apache.arrow.memory.RootAllocator;

import java.util.HashMap;
import java.util.Map;

public class KasanariLanceCatalogFactory implements LanceCatalogFactory {
    @Override
    public LanceCatalogAdapter create(
            String implementation,
            Map<String, String> fileIoProperties,
            Map<String, String> catalogProperties
    ) {
        var catalog = new KasanariLanceCatalog();
        var mergedProperties = new HashMap<>(fileIoProperties);
        mergedProperties.putAll(catalogProperties);

        catalog.initialize(mergedProperties, new RootAllocator());

        return new DefaultLanceCatalogAdapter(catalog);
    }
}
