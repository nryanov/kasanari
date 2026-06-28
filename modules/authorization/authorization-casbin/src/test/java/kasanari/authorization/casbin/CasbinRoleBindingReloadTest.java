package kasanari.authorization.casbin;

import kasanari.authorization.spi.Permission;
import kasanari.repository.management.security.model.StoredRoleBinding;
import org.casbin.jcasbin.main.Enforcer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CasbinRoleBindingReloadTest {
    private Enforcer enforcer;

    @BeforeEach
    void setUp() {
        enforcer = CasbinEnforcerFactory.createEnforcer();
    }

    @Test
    void reloadReplacesExistingPolicies() {
        enforcer.addPolicy("alice", "ICEBERG/old", Permission.IcebergTableGet.wireName());

        CasbinBindingPolicyExpander.reloadBindingPolicies(enforcer, List.of(
                new StoredRoleBinding("alice", CasbinRoles.ICEBERG_CATALOG_VIEWER, "ICEBERG/new")
        ));

        assertFalse(enforcer.enforce("alice", "ICEBERG/old/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertTrue(enforcer.enforce("alice", "ICEBERG/new/analytics/orders", Permission.IcebergTableGet.wireName()));
    }

    @Test
    void reloadExpandsEachBindingIntoPermissionPolicies() {
        CasbinBindingPolicyExpander.reloadBindingPolicies(enforcer, List.of(
                new StoredRoleBinding("alice", CasbinRoles.ICEBERG_CATALOG_VIEWER, "ICEBERG/prod"),
                new StoredRoleBinding("bob", CasbinRoles.PAIMON_CATALOG_EDITOR, "PAIMON/events")
        ));

        assertTrue(enforcer.enforce("alice", "ICEBERG/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertTrue(enforcer.enforce("bob", "PAIMON/events/ns1/users", Permission.PaimonTableCreate.wireName()));
        assertFalse(enforcer.getPolicy().isEmpty());
    }
}
