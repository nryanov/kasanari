package kasanari.catalog.iceberg;

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
