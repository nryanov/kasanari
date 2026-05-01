package kasanari.catalog.paimon;

import org.apache.paimon.catalog.CatalogFactory;
import org.apache.paimon.jdbc.JdbcCatalog;

public class JdbcPaimonCatalogFactory implements PaimonCatalogFactory {
    public void foo() {
//        new JdbcCatalog()
        var catalog = CatalogFactory.createCatalog(null);
    }
}
