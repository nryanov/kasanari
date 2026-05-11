package kasanari.catalog.iceberg;

import java.util.Map;

public interface IcebergCatalogFactory {
    IcebergCatalogAdapter create(
            String name,
            Map<String, String> hadoopProperties,
            Map<String, String> catalogProperties
    );
}
