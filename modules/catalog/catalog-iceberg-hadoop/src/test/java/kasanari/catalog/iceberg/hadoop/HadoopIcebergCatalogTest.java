package kasanari.catalog.iceberg.hadoop;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapterTest;
import org.junit.jupiter.api.Disabled;

import java.util.Map;

@Disabled("getSubject is supported only if a security manager is allowed. Java version <= 17")
public class HadoopIcebergCatalogTest extends IcebergCatalogAdapterTest {
    @Override
    public IcebergCatalogAdapter setupCatalog() {
        var factory = new HadoopIcebergCatalogFactory();
        return factory.create(Map.of(
                "warehouse", "file://hadoop-catalog-test"
        ));
    }
}
