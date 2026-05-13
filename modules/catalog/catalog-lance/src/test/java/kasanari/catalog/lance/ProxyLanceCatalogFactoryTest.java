package kasanari.catalog.lance;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProxyLanceCatalogFactoryTest {
    @Test
    void registersHiveAliasesAndCanBuildDirCatalog() throws Exception {
        var factory = new ProxyLanceCatalogFactory();

        assertDoesNotThrow(() -> Class.forName("org.lance.namespace.hive2.Hive2Namespace"));
        assertDoesNotThrow(() -> Class.forName("org.lance.namespace.hive3.Hive3Namespace"));
        assertTrue(org.lance.namespace.LanceNamespace.isRegistered(ProxyLanceCatalogFactory.HIVE2_ALIAS));
        assertTrue(org.lance.namespace.LanceNamespace.isRegistered(ProxyLanceCatalogFactory.HIVE3_ALIAS));

        try (var adapter = factory.create("dir", Map.of("root", "build/tmp/lance-proxy-test"))) {
            assertNotNull(adapter.delegate());
        }
    }
}
