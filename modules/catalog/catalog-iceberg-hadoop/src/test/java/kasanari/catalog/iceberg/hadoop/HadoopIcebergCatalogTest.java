package kasanari.catalog.iceberg.hadoop;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapterTest;

import java.util.Map;

public class HadoopIcebergCatalogTest extends IcebergCatalogAdapterTest {
    @Override
    public IcebergCatalogAdapter setupCatalog() {
        var factory = new HadoopIcebergCatalogFactory();
        return factory.create(Map.of(
                "warehouse", "file:///tmp/hadoop-catalog-test"
        ));
    }
}
