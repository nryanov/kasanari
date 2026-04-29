package kasanari.catalog.iceberg.inmemory;


import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapterTest;
import org.apache.iceberg.catalog.Namespace;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryIcebergCatalogTest extends IcebergCatalogAdapterTest {
    private IcebergCatalogAdapter adapter;
    private final AtomicInteger namespaceId = new AtomicInteger(1);

    @Override
    public IcebergCatalogAdapter setupCatalog() {
        var factory = new InMemoryIcebergCatalogFactory();
        this.adapter = factory.create(Map.of());
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
