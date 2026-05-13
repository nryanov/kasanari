package kasanari.catalog.lance;

import org.apache.arrow.memory.RootAllocator;
import org.lance.namespace.LanceNamespace;

import java.util.HashMap;
import java.util.Map;

public class ProxyLanceCatalogFactory implements LanceCatalogFactory {
    public static final String HIVE2_ALIAS = "hive2";
    public static final String HIVE3_ALIAS = "hive3";

    static {
        LanceNamespace.registerNamespaceImpl(HIVE2_ALIAS, "org.lance.namespace.hive2.Hive2Namespace");
        LanceNamespace.registerNamespaceImpl(HIVE3_ALIAS, "org.lance.namespace.hive3.Hive3Namespace");
    }

    @Override
    public LanceCatalogAdapter create(String implementation, Map<String, String> properties) {
        var config = new HashMap<String, String>();
        if (properties != null) {
            config.putAll(properties);
        }

        var namespace = LanceNamespace.connect(implementation, config, new RootAllocator());
        return new DefaultLanceCatalogAdapter(namespace);
    }
}
