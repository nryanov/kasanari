package kasanari.catalog.iceberg.kasanari;

import kasanari.catalog.iceberg.core.DefaultIcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogFactory;

import java.util.Map;

public class KasanariIcebergCatalogFactory implements IcebergCatalogFactory {
    @Override
    public IcebergCatalogAdapter create(Map<String, String> properties) {
        var catalog = new KasanariCatalog();
        catalog.initialize("kasanari", properties);
        return new DefaultIcebergCatalogAdapter(catalog);
    }
}
