package kasanari.catalog.lance;

import org.apache.arrow.memory.RootAllocator;

import java.util.Map;

public class KasanariLanceCatalogFactory implements LanceCatalogFactory {
    @Override
    public LanceCatalogAdapter create(String implementation, Map<String, String> properties) {
        var catalog = new KasanariLanceCatalog();
        catalog.initialize(properties, new RootAllocator());
        return new DefaultLanceCatalogAdapter(catalog);
    }
}
