package kasanari.catalog.iceberg.jdbc;

import kasanari.catalog.iceberg.core.DefaultIcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogFactory;
import org.apache.iceberg.jdbc.JdbcCatalog;

import java.util.Map;

public class JdbcIcebergCatalogFactory implements IcebergCatalogFactory {
    @Override
    public IcebergCatalogAdapter create(Map<String, String> properties) {
        var catalog = new JdbcCatalog();
        catalog.initialize("jdbc", properties);
        return new DefaultIcebergCatalogAdapter(catalog);
    }
}
