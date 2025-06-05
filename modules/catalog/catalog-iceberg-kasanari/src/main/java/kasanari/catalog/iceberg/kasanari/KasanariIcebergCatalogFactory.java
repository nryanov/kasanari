package kasanari.catalog.iceberg.kasanari;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogFactory;
import kasanari.catalog.iceberg.core.LoggedIcebergCatalogAdapter;

import java.util.Map;

public class KasanariIcebergCatalogFactory implements IcebergCatalogFactory {
    @Override
    public IcebergCatalogAdapter create(Map<String, String> properties) {
        var catalog = new KasanariCatalog();
        catalog.initialize("kasanari", properties);
        // todo: allow to configure adapter properties
        return new LoggedIcebergCatalogAdapter(new KasanariIcebergCatalogAdapter(catalog));
    }
}
