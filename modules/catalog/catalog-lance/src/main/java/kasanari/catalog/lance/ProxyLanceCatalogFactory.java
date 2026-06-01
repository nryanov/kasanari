package kasanari.catalog.lance;

import org.apache.arrow.memory.RootAllocator;
import org.lance.namespace.LanceNamespace;

import java.util.HashMap;
import java.util.Map;

public class ProxyLanceCatalogFactory implements LanceCatalogFactory {

    @Override
    public LanceCatalogAdapter create(
            String implementation,
            Map<String, String> fileIoProperties,
            Map<String, String> catalogProperties
    ) {
        var mergedProperties = new HashMap<>(fileIoProperties);
        mergedProperties.putAll(catalogProperties);
        var namespace = LanceNamespace.connect(implementation, mergedProperties, new RootAllocator());
        
        return new DefaultLanceCatalogAdapter(namespace);
    }
}
