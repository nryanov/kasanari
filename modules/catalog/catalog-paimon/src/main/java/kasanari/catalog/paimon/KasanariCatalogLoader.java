package kasanari.catalog.paimon;

import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.CatalogContext;
import org.apache.paimon.catalog.CatalogLoader;
import org.apache.paimon.fs.FileIO;

public class KasanariCatalogLoader implements CatalogLoader {
    private final FileIO fileIO;
    private final String catalogKey;
    private final CatalogContext context;
    private final String warehouse;

    public KasanariCatalogLoader(FileIO fileIO, String catalogKey, CatalogContext context, String warehouse) {
        this.fileIO = fileIO;
        this.catalogKey = catalogKey;
        this.context = context;
        this.warehouse = warehouse;
    }

    @Override
    public Catalog load() {
        return new KasanariPaimonCatalog(fileIO, catalogKey, context, warehouse);
    }
}
