package kasanari.catalog.iceberg.rest;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogFactory;
import org.apache.iceberg.rest.RESTCatalog;

import java.util.Map;

public class RestIcebergCatalogFactory implements IcebergCatalogFactory {
    @Override
    public IcebergCatalogAdapter create(Map<String, String> properties) {
        var catalog = new RESTCatalog();
        return null;
    }
}
