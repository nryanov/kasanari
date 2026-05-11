package kasanari.catalog.iceberg;


import java.util.Map;

public class KasanariIcebergCatalogFactory implements IcebergCatalogFactory {
    @Override
    public IcebergCatalogAdapter create(Map<String, String> properties) {
        var catalog = new KasanariIcebergCatalog();
        catalog.initialize("kasanari", properties);
        // todo: allow to configure adapter properties
        return new LoggedIcebergCatalogAdapter(new KasanariIcebergCatalogAdapter(catalog));
    }
}
