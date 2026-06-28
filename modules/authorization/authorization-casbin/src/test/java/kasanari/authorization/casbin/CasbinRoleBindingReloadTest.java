package kasanari.authorization.casbin;

import kasanari.authorization.spi.Permission;
import kasanari.repository.management.security.model.StoredRoleBinding;
import org.casbin.jcasbin.main.Enforcer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CasbinRoleBindingReloadTest {
    private Enforcer enforcer;

    @BeforeEach
    void setUp() {
        enforcer = CasbinEnforcerFactory.createEnforcer();
    }

    @Test
    void reloadReplacesExistingPolicies() {
        enforcer.addPolicy("alice", "iceberg/old", Permission.IcebergTableGet.wireName());

        CasbinBindingPolicyExpander.reloadBindingPolicies(enforcer, List.of(
                new StoredRoleBinding("alice", "iceberg/new", CasbinRoles.ICEBERG_CATALOG_VIEWER)
        ));

        assertFalse(enforcer.enforce("alice", "iceberg/old/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertTrue(enforcer.enforce("alice", "iceberg/new/analytics/orders", Permission.IcebergTableGet.wireName()));
    }

    @Test
    void reloadExpandsEachBindingIntoPermissionPolicies() {
        CasbinBindingPolicyExpander.reloadBindingPolicies(enforcer, List.of(
                new StoredRoleBinding("alice", "iceberg/prod", CasbinRoles.ICEBERG_CATALOG_VIEWER),
                new StoredRoleBinding("bob", "paimon/events", CasbinRoles.PAIMON_CATALOG_EDITOR)
        ));

        assertTrue(enforcer.enforce("alice", "iceberg/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertTrue(enforcer.enforce("bob", "paimon/events/ns1/users", Permission.PaimonTableCreate.wireName()));
        assertFalse(enforcer.getPolicy().isEmpty());
    }
}
