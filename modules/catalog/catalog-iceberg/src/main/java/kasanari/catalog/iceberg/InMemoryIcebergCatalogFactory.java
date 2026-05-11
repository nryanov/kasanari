package kasanari.catalog.iceberg;

import org.apache.iceberg.inmemory.InMemoryCatalog;

import java.util.Map;

public class InMemoryIcebergCatalogFactory implements IcebergCatalogFactory {
    @Override
    public IcebergCatalogAdapter create(Map<String, String> properties) {
        var catalog = new InMemoryCatalog();
        catalog.initialize("inmemory", properties);
        return new DefaultIcebergCatalogAdapter(catalog);
    }
}
