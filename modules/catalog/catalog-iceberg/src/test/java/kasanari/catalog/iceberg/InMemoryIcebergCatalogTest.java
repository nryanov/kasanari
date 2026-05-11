package kasanari.catalog.iceberg;


import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.inmemory.InMemoryCatalog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryIcebergCatalogTest extends IcebergCatalogAdapterTest {
    private IcebergCatalogAdapter adapter;
    private final AtomicInteger namespaceId = new AtomicInteger(1);

    @Override
    public IcebergCatalogAdapter setupCatalog() {
        var properties = new HashMap<String, String>();
        properties.put(CatalogProperties.CATALOG_IMPL, InMemoryCatalog.class.getName());

        var factory = new ProxyIcebergCatalogFactory();
        this.adapter = factory.create("inmemory", Map.of(), properties);
        return adapter;
    }

    @Override
    public String entityLocation(String name) {
        return "memory";
    }

    @Override
    public String tableName() {
        return "table";
    }

    @Override
    public String viewName() {
        return "view";
    }

    @Override
    public Namespace namespaceName() {
        var ns = Namespace.of("ns_" + namespaceId.getAndIncrement());
        adapter.createNamespace(ns);
        return ns;
    }
}
