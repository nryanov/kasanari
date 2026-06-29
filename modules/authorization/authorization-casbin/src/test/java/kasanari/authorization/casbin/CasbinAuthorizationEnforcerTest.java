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

    @Test
    void viewerRoleExpansionAllowsReadOnlyOperations() {
        enforcer = CasbinBindingPolicyExpander.buildEnforcer(List.of(
                new StoredRoleBinding("alice", "iceberg/prod", CasbinRoles.ICEBERG_CATALOG_VIEWER)
        ));

        assertTrue(enforcer.enforce("alice", "iceberg/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertTrue(enforcer.enforce("alice", "iceberg/prod/analytics", Permission.IcebergNamespaceList.wireName()));
        assertFalse(enforcer.enforce("alice", "iceberg/prod/analytics/orders", Permission.IcebergTableCreate.wireName()));
    }

    @Test
    void editorRoleExpansionAllowsMutations() {
        enforcer = CasbinBindingPolicyExpander.buildEnforcer(List.of(
                new StoredRoleBinding("alice", "paimon/events", CasbinRoles.PAIMON_CATALOG_EDITOR)
        ));

        assertTrue(enforcer.enforce("alice", "paimon/events/ns1/users", Permission.PaimonTableCreate.wireName()));
        assertFalse(enforcer.enforce("alice", "paimon/events/ns1/users", Permission.PaimonCatalogDelete.wireName()));
    }

    @Test
    void adminRoleExpansionIncludesRoleBindingAdministration() {
        enforcer = CasbinBindingPolicyExpander.buildEnforcer(List.of(
                new StoredRoleBinding("root", "lance/lake", CasbinRoles.LANCE_CATALOG_ADMIN)
        ));

        assertTrue(enforcer.enforce("root", "lance/lake/ns1/users", Permission.RoleBindingAdd.wireName()));
        assertTrue(enforcer.enforce("root", "lance/lake/ns1/users", Permission.RoleBindingGet.wireName()));
        assertTrue(enforcer.enforce("root", "lance/lake/ns1/users", Permission.RoleBindingDelete.wireName()));
        assertTrue(enforcer.enforce("root", "lance/lake/ns1/users", Permission.LanceTableDrop.wireName()));
    }

    @Test
    void editorRoleExpansionDoesNotIncludeRoleBindingAdministration() {
        enforcer = CasbinBindingPolicyExpander.buildEnforcer(List.of(
                new StoredRoleBinding("alice", "iceberg/prod", CasbinRoles.ICEBERG_CATALOG_EDITOR)
        ));

        assertFalse(enforcer.enforce("alice", "iceberg/prod/analytics", Permission.RoleBindingAdd.wireName()));
        assertFalse(enforcer.enforce("alice", "iceberg/prod/analytics", Permission.RoleBindingGet.wireName()));
        assertFalse(enforcer.enforce("alice", "iceberg/prod/analytics", Permission.RoleBindingDelete.wireName()));
    }

    @Test
    void unknownRoleProducesNoPolicies() {
        enforcer = CasbinBindingPolicyExpander.buildEnforcer(List.of(
                new StoredRoleBinding("alice", "iceberg/prod", "UnknownRole")
        ));

        assertFalse(enforcer.enforce("alice", "iceberg/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
    }

    @Test
    void buildEnforcerProducesIndependentSnapshot() {
        var previous = CasbinBindingPolicyExpander.buildEnforcer(List.of());
        previous.addPolicy("alice", "iceberg/old", Permission.IcebergTableGet.wireName());

        enforcer = CasbinBindingPolicyExpander.buildEnforcer(List.of(
                new StoredRoleBinding("alice", "iceberg/new", CasbinRoles.ICEBERG_CATALOG_VIEWER)
        ));

        assertTrue(previous.enforce("alice", "iceberg/old/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertFalse(enforcer.enforce("alice", "iceberg/old/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertTrue(enforcer.enforce("alice", "iceberg/new/analytics/orders", Permission.IcebergTableGet.wireName()));
    }

    @Test
    void buildEnforcerExpandsEachBindingIntoPermissionPolicies() {
        enforcer = CasbinBindingPolicyExpander.buildEnforcer(List.of(
                new StoredRoleBinding("alice", "iceberg/prod", CasbinRoles.ICEBERG_CATALOG_VIEWER),
                new StoredRoleBinding("bob", "paimon/events", CasbinRoles.PAIMON_CATALOG_EDITOR)
        ));

        assertTrue(enforcer.enforce("alice", "iceberg/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertTrue(enforcer.enforce("bob", "paimon/events/ns1/users", Permission.PaimonTableCreate.wireName()));
        assertFalse(enforcer.getPolicy().isEmpty());
    }

    @Test
    void policyHolderSwapUpdatesActiveEnforcer() {
        var holder = new CasbinPolicyHolder();
        holder.swap(CasbinBindingPolicyExpander.buildEnforcer(List.of(
                new StoredRoleBinding("alice", "iceberg/prod", CasbinRoles.ICEBERG_CATALOG_VIEWER)
        )));

        var beforeSwap = holder.current();
        holder.swap(CasbinBindingPolicyExpander.buildEnforcer(List.of(
                new StoredRoleBinding("bob", "paimon/events", CasbinRoles.PAIMON_CATALOG_EDITOR)
        )));

        assertTrue(beforeSwap.enforce("alice", "iceberg/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertFalse(holder.current().enforce("alice", "iceberg/prod/analytics/orders", Permission.IcebergTableGet.wireName()));
        assertTrue(holder.current().enforce("bob", "paimon/events/ns1/users", Permission.PaimonTableCreate.wireName()));
    }
}
