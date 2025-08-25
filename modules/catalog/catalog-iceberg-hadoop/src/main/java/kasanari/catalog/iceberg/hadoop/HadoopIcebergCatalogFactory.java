package kasanari.catalog.iceberg.hadoop;

import kasanari.catalog.iceberg.core.DefaultIcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.hadoop.HadoopCatalog;

import java.util.Map;

public class HadoopIcebergCatalogFactory implements IcebergCatalogFactory {
    @Override
    public IcebergCatalogAdapter create(Map<String, String> properties) {
        var catalog = new HadoopCatalog(
                new Configuration(), properties.get("warehouse")
        );
        catalog.initialize("hadoop", properties);
        return new DefaultIcebergCatalogAdapter(catalog);
    }
}
