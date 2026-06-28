package kasanari.authorization.casbin;

import kasanari.authorization.spi.Permission;
import org.casbin.jcasbin.main.Enforcer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CasbinResourceInheritanceTest {
    private Enforcer enforcer;

    @BeforeEach
    void setUp() {
        enforcer = CasbinEnforcerFactory.createEnforcer();
    }

    @Test
    void catalogScopeInheritsNamespaceAndTableAccess() {
        enforcer.addPolicy("alice", "iceberg/prod", Permission.IcebergTableGet.wireName());

        assertTrue(enforcer.enforce("alice", "iceberg/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertFalse(enforcer.enforce("alice", "iceberg/other/analytics/orders", Permission.IcebergTableGet.wireName()));
    }

    @Test
    void namespaceScopeInheritsTableAccess() {
        enforcer.addPolicy("alice", "iceberg/prod/analytics", Permission.IcebergTableGet.wireName());

        assertTrue(enforcer.enforce("alice", "iceberg/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertFalse(enforcer.enforce("alice", "iceberg/prod/other/orders", Permission.IcebergTableGet.wireName()));
    }

    @Test
    void engineScopeInheritsAllCatalogPaths() {
        enforcer.addPolicy("alice", "iceberg", Permission.IcebergTableGet.wireName());

        assertTrue(enforcer.enforce("alice", "iceberg/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertFalse(enforcer.enforce("alice", "paimon/events/users", Permission.PaimonTableGet.wireName()));
    }

    @Test
    void permissionPatternUsesGlobMatch() {
        enforcer.addPolicy("alice", "iceberg/prod", "Iceberg*Get");

        assertTrue(enforcer.enforce("alice", "iceberg/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertFalse(enforcer.enforce("alice", "iceberg/prod/analytics/orders", Permission.IcebergTableCreate.wireName()));
    }
}
