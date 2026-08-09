package kasanari.catalog.paimon;

import org.apache.hadoop.conf.Configuration;
import org.apache.paimon.catalog.CatalogContext;
import org.apache.paimon.catalog.CatalogFactory;
import org.apache.paimon.options.Options;

import java.util.Map;

public class ProxyPaimonCatalogFactory implements PaimonCatalogFactory {
    @Override
    public PaimonCatalogAdapter create(String name, Map<String, String> fileIoProperties, Map<String, String> properties) {
        var config = new Configuration();
        fileIoProperties.forEach(config::set);

        var options = new Options();
        properties.forEach(options::set);

        var catalogContext = CatalogContext.create(options, config);
        var catalog = CatalogFactory.createCatalog(catalogContext);

        return new DefaultPaimonCatalogAdapter(catalog);
    }
}
