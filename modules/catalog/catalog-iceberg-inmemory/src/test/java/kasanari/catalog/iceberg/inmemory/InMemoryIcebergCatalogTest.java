package kasanari.catalog.iceberg.inmemory;


import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapterTest;

import java.util.Map;

public class InMemoryIcebergCatalogTest extends IcebergCatalogAdapterTest {
    @Override
    public IcebergCatalogAdapter setupCatalog() {
        var factory = new InMemoryIcebergCatalogFactory();
        return factory.create(Map.of());
    }
}
