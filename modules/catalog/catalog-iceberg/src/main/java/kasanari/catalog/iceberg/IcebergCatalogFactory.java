package kasanari.catalog.iceberg;

import java.util.Map;

public interface IcebergCatalogFactory {
    IcebergCatalogAdapter create(Map<String, String> properties);
}
