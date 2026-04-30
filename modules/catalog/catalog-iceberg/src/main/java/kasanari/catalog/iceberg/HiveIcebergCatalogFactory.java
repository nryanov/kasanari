package kasanari.catalog.iceberg;

import org.apache.iceberg.hive.HiveCatalog;

import java.util.Map;

public class HiveIcebergCatalogFactory implements IcebergCatalogFactory {
    @Override
    public IcebergCatalogAdapter create(Map<String, String> properties) {
        var catalog = new HiveCatalog();
        catalog.initialize("hive", properties);
        return new DefaultIcebergCatalogAdapter(catalog);
    }
}
