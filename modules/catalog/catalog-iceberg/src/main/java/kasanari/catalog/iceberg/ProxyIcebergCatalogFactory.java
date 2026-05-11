package kasanari.catalog.iceberg;

import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.CatalogUtil;

import java.util.Map;

public class ProxyIcebergCatalogFactory implements IcebergCatalogFactory {
    @Override
    public IcebergCatalogAdapter create(
            String name,
            Map<String, String> hadoopProperties,
            Map<String, String> catalogProperties
    ) {
        var hadoopConfiguration = new Configuration();
        hadoopProperties.forEach(hadoopConfiguration::set);

        var catalog = CatalogUtil.buildIcebergCatalog(name, catalogProperties, hadoopConfiguration);
        return new DefaultIcebergCatalogAdapter(catalog);
    }
}
