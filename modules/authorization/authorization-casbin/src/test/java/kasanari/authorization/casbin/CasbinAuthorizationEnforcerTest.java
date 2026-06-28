package kasanari.authorization.casbin;

import kasanari.authorization.spi.Permission;
import kasanari.repository.management.security.model.StoredRoleBinding;
import org.casbin.jcasbin.main.Enforcer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CasbinAuthorizationEnforcerTest {
    private Enforcer enforcer;

    @BeforeEach
    void setUp() {
        enforcer = CasbinEnforcerFactory.createEnforcer();
    }

    @Test
    void viewerRoleExpansionAllowsReadOnlyOperations() {
        CasbinBindingPolicyExpander.reloadBindingPolicies(enforcer, List.of(
                new StoredRoleBinding("alice", CasbinRoles.ICEBERG_CATALOG_VIEWER, "ICEBERG/prod")
        ));

        assertTrue(enforcer.enforce("alice", "ICEBERG/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertTrue(enforcer.enforce("alice", "ICEBERG/prod/analytics", Permission.IcebergNamespaceList.wireName()));
        assertFalse(enforcer.enforce("alice", "ICEBERG/prod/analytics/orders", Permission.IcebergTableCreate.wireName()));
    }

    @Test
    void editorRoleExpansionAllowsMutations() {
        CasbinBindingPolicyExpander.reloadBindingPolicies(enforcer, List.of(
                new StoredRoleBinding("alice", CasbinRoles.ICEBERG_CATALOG_EDITOR, "PAIMON/events")
        ));

        assertTrue(enforcer.enforce("alice", "PAIMON/events/ns1/users", Permission.PaimonTableCreate.wireName()));
        assertFalse(enforcer.enforce("alice", "PAIMON/events/ns1/users", Permission.PaimonCatalogDelete.wireName()));
    }

    @Test
    void adminRoleExpansionIncludesRoleBindingAdministration() {
        CasbinBindingPolicyExpander.reloadBindingPolicies(enforcer, List.of(
                new StoredRoleBinding("root", CasbinRoles.LANCE_CATALOG_ADMIN, "LANCE/lake")
        ));

        assertTrue(enforcer.enforce("root", "LANCE/lake/ns1/users", Permission.RoleBindingAdd.wireName()));
        assertTrue(enforcer.enforce("root", "LANCE/lake/ns1/users", Permission.RoleBindingGet.wireName()));
        assertTrue(enforcer.enforce("root", "LANCE/lake/ns1/users", Permission.RoleBindingDelete.wireName()));
        assertTrue(enforcer.enforce("root", "LANCE/lake/ns1/users", Permission.LanceTableDrop.wireName()));
    }

    @Test
    void editorRoleExpansionDoesNotIncludeRoleBindingAdministration() {
        CasbinBindingPolicyExpander.reloadBindingPolicies(enforcer, List.of(
                new StoredRoleBinding("alice", CasbinRoles.ICEBERG_CATALOG_EDITOR, "ICEBERG/prod")
        ));

        assertFalse(enforcer.enforce("alice", "ICEBERG/prod/analytics", Permission.RoleBindingAdd.wireName()));
        assertFalse(enforcer.enforce("alice", "ICEBERG/prod/analytics", Permission.RoleBindingGet.wireName()));
        assertFalse(enforcer.enforce("alice", "ICEBERG/prod/analytics", Permission.RoleBindingDelete.wireName()));
    }

    @Test
    void unknownRoleProducesNoPolicies() {
        CasbinBindingPolicyExpander.reloadBindingPolicies(enforcer, List.of(
                new StoredRoleBinding("alice", "UnknownRole", "ICEBERG/prod")
        ));

        assertFalse(enforcer.enforce("alice", "ICEBERG/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
    }
}
