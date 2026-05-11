package kasanari.catalog.iceberg;

import org.apache.iceberg.rest.RESTCatalog;

import java.util.Map;

// TODO: implement
public class RestIcebergCatalogFactory implements IcebergCatalogFactory {
    @Override
    public IcebergCatalogAdapter create(Map<String, String> properties) {
        var catalog = new RESTCatalog();
        return null;
    }
}
