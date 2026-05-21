package kasanari.catalog.iceberg;


import org.apache.hadoop.conf.Configuration;

import java.util.Map;

public class KasanariIcebergCatalogFactory implements IcebergCatalogFactory {
    @Override
    public IcebergCatalogAdapter create(String name, Map<String, String> hadoopProperties, Map<String, String> catalogProperties) {
        var catalog = new KasanariIcebergCatalog();

        var hadoopConfiguration = new Configuration();
        hadoopProperties.forEach(hadoopConfiguration::set);

        catalog.setConf(hadoopConfiguration);
        catalog.initialize(name, catalogProperties);

        return new KasanariIcebergCatalogAdapter(catalog);
    }
}
