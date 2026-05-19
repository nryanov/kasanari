package kasanari.catalog.lance;

import org.apache.arrow.memory.RootAllocator;
import org.lance.namespace.LanceNamespace;

import java.util.Map;

public class ProxyLanceCatalogFactory implements LanceCatalogFactory {

    @Override
    public LanceCatalogAdapter create(
            String implementation,
            Map<String, String> fileIoProperties,
            Map<String, String> catalogProperties
    ) {
        var namespace = LanceNamespace.connect(implementation, catalogProperties, new RootAllocator());
        
        return new DefaultLanceCatalogAdapter(namespace);
    }
}
