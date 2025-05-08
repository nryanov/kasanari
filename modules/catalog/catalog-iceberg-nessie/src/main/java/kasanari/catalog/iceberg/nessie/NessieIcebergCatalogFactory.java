package kasanari.catalog.iceberg.nessie;

import kasanari.catalog.iceberg.core.DefaultIcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogFactory;
import org.apache.iceberg.nessie.NessieCatalog;

import java.util.Map;

public class NessieIcebergCatalogFactory implements IcebergCatalogFactory {
    @Override
    public IcebergCatalogAdapter create(Map<String, String> properties) {
        var catalog = new NessieCatalog();
        catalog.initialize("nessie", properties);
        return new DefaultIcebergCatalogAdapter(catalog);
    }
}
