package kasanari.authorization.casbin;

import kasanari.authorization.spi.Permission;
import org.casbin.jcasbin.main.Enforcer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CasbinResourceInheritanceTest {
    private Enforcer enforcer;

    @BeforeEach
    void setUp() {
        enforcer = CasbinEnforcerFactory.createEnforcer();
    }

    @Test
    void catalogScopeInheritsNamespaceAndTableAccess() {
        enforcer.addPolicy("alice", "ICEBERG/prod/*", Permission.IcebergTableGet.wireName());

        assertTrue(enforcer.enforce("alice", "ICEBERG/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertFalse(enforcer.enforce("alice", "ICEBERG/other/analytics/orders", Permission.IcebergTableGet.wireName()));
    }

    @Test
    void namespaceScopeInheritsTableAccess() {
        enforcer.addPolicy("alice", "ICEBERG/prod/analytics/*", Permission.IcebergTableGet.wireName());

        assertTrue(enforcer.enforce("alice", "ICEBERG/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertFalse(enforcer.enforce("alice", "ICEBERG/prod/other/orders", Permission.IcebergTableGet.wireName()));
    }

    @Test
    void permissionPatternUsesGlobMatch() {
        enforcer.addPolicy("alice", "ICEBERG/prod/*", "Iceberg*Get");

        assertTrue(enforcer.enforce("alice", "ICEBERG/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertFalse(enforcer.enforce("alice", "ICEBERG/prod/analytics/orders", Permission.IcebergTableCreate.wireName()));
    }
}
