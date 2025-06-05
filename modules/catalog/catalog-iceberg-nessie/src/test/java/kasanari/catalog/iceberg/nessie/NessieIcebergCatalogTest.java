package kasanari.catalog.iceberg.nessie;

import kasanari.catalog.iceberg.core.IcebergCatalogAdapter;
import kasanari.catalog.iceberg.core.IcebergCatalogAdapterTest;

import org.projectnessie.testing.nessie.ImmutableNessieConfig;
import org.projectnessie.testing.nessie.NessieContainer;

import java.util.Map;

public class NessieIcebergCatalogTest extends IcebergCatalogAdapterTest {
//    private static final NessieContainer nessieContainer = new NessieContainer(ImmutableNessieConfig.builder().build());

    @Override
    public IcebergCatalogAdapter setupCatalog() {
//        nessieContainer.start();

        var factory = new NessieIcebergCatalogFactory();
        return factory.create(Map.of(
                "uri", "http://localhost:19120/api/v2",
                "warehouse", "file:///tmp/nessie-warehouse",
                "ref", "main"
        ));
    }

    @Override
    public void close() {
//        nessieContainer.close();
    }
}
