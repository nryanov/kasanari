package kasanari.catalog.iceberg;


import org.apache.iceberg.CatalogProperties;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.inmemory.InMemoryCatalog;

import java.util.HashMap;
import java.util.Map;

public class InMemoryIcebergCatalogTest extends IcebergCatalogAdapterTest {
    @Override
    public IcebergCatalogAdapter setupCatalog() {
        var properties = new HashMap<String, String>();
        properties.put(CatalogProperties.CATALOG_IMPL, InMemoryCatalog.class.getName());

        var factory = new ProxyIcebergCatalogFactory();
        return factory.create("inmemory", Map.of(), properties);
    }

    @Override
    public String entityLocation(TableIdentifier identifier) {
        return "memory";
    }
}
