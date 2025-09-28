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
        var hadoopConfiguration = new Configuration();
        properties.forEach(hadoopConfiguration::set);

        var catalog = new HadoopCatalog(hadoopConfiguration, properties.get("warehouse"));
        return new DefaultIcebergCatalogAdapter(catalog);
    }
}
