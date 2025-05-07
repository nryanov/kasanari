package kasanari.catalog.iceberg.core;

import java.util.Map;

public interface IcebergCatalogFactory {
    IcebergCatalogAdapter create(Map<String, String> properties);
}
