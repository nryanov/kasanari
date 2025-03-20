package kasanari.catalog.iceberg.nessie;

import org.apache.iceberg.CatalogUtil;
import org.apache.iceberg.nessie.NessieCatalog;

public class DefaultNessieCatalog {
    public static void main(String[] args) {
        var catalog = new NessieCatalog();
        var table = catalog.loadTable(null);

        CatalogUtil.buildIcebergCatalog(null, null, null);


    }
}
