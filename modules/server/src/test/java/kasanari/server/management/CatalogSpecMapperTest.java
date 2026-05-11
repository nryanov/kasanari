package kasanari.server.management;

import kasanari.catalog.management.model.CatalogMode;
import kasanari.catalog.management.model.CatalogSpec;
import kasanari.catalog.management.model.CatalogType;
import kasanari.catalog.management.model.IcebergCatalogSpecModeConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class CatalogSpecMapperTest {
    @Test
    void shouldStripSecretsFromSanitizedSpec() {
        var spec = new CatalogSpec();
        spec.setType(CatalogSpec.TypeEnum.ICEBERG);

        var mode = new IcebergCatalogSpecModeConfig();
        mode.setProperties(Map.of("warehouse", "s3://warehouse"));
        mode.setSecrets(Map.of("token", "secret"));
        spec.setModeConfig(mode);

        CatalogSpecMapper.validate(CatalogType.ICEBERG, CatalogMode.INTERNAL, spec);

        var secrets = CatalogSpecMapper.extractSecrets(spec);
        var sanitized = CatalogSpecMapper.toSanitized(spec);
        var publicSpec = CatalogSpecMapper.toPublic(sanitized);

        Assertions.assertEquals("secret", secrets.get("token"));
        Assertions.assertTrue(sanitized.getModeConfig().getSecrets().isEmpty());
        Assertions.assertEquals("s3://warehouse", publicSpec.getModeConfig().getProperties().get("warehouse"));
    }

    @Test
    void shouldValidateProxyModeEndpointRequirement() {
        var spec = new CatalogSpec();
        spec.setType(CatalogSpec.TypeEnum.LANCE);

        var mode = new IcebergCatalogSpecModeConfig();
        mode.setProperties(Map.of());
        spec.setModeConfig(mode);

        var error = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> CatalogSpecMapper.validate(CatalogType.LANCE, CatalogMode.PROXY, spec)
        );
        Assertions.assertTrue(error.getMessage().contains("requires"));
    }
}
