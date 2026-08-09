package kasanari.catalog.paimon;

import org.apache.hadoop.conf.Configuration;
import org.apache.paimon.catalog.CatalogContext;
import org.apache.paimon.fs.FileIO;
import org.apache.paimon.fs.Path;
import org.apache.paimon.options.CatalogOptions;
import org.apache.paimon.options.Options;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

public class KasanariPaimonCatalogFactory implements PaimonCatalogFactory {
    public static final String CATALOG_NAME = "kasanari.catalog.name";
    private static final String DEFAULT_CATALOG_NAME = "default";

    @Override
    public PaimonCatalogAdapter create(Map<String, String> fileIoProperties, Map<String, String> properties) {
        var configuration = new Configuration();
        fileIoProperties.forEach(configuration::set);

        var options = new Options();
        properties.forEach(options::set);

        var catalogContext = CatalogContext.create(options, configuration);
        var warehouse = options.get(CatalogOptions.WAREHOUSE);
        if (warehouse == null || warehouse.isBlank()) {
            throw new IllegalArgumentException("Required key `warehouse` is not set");
        }

        var warehousePath = new Path(warehouse);
        var catalogName = properties.getOrDefault(CATALOG_NAME, DEFAULT_CATALOG_NAME);

        try {
            FileIO fileIO = FileIO.get(warehousePath, catalogContext);
            fileIO.checkOrMkdirs(warehousePath);
            var catalog = new KasanariPaimonCatalog(fileIO, catalogName, catalogContext, warehouse);
            return new DefaultPaimonCatalogAdapter(catalog);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
