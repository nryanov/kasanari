package kasanari.management.security;

import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.management.model.CatalogType;
import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.JdbcTransactionManager;
import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.repository.management.security.RoleBindingRepository;
import kasanari.repository.management.security.model.StoredRoleBinding;
import kasanari.repository.management.security.postgres.JdbcManagementSecurityQueries;
import kasanari.repository.management.security.postgres.JdbcRoleBindingRepository;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.jdbi.v3.core.Handle;

import java.util.List;

public class ManagementSecurityService {
    private final TransactionManager<Handle> txManager;
    private final RoleBindingRepository<Handle> roleBindingRepository;
    private final Enforcer enforcer;

    public ManagementSecurityService(KasanariDataSource dataSource) {
        this.txManager = new JdbcTransactionManager(dataSource);
        this.roleBindingRepository = new JdbcRoleBindingRepository();
        this.enforcer = createEnforcer();
        initSchema();
        initRolePermissions();
        reloadGroupingPolicies();
    }

    public void deleteRoles(List<StoredRoleBinding> bindings) {
        txManager.inTransaction(tx -> roleBindingRepository.delete(tx, bindings));
    }

    public void updateRoles(List<StoredRoleBinding> bindings) {
        txManager.inTransaction(tx -> roleBindingRepository.upsert(tx, bindings));
    }

    public List<StoredRoleBinding> listRoles(String subject, CatalogType catalogType) {
        return txManager.inTransactionR(tx -> roleBindingRepository.list(tx, subject, catalogType));
    }

    public String subject(SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return "anonymous";
        }
        return securityContext.getUserPrincipal().getName();
    }

    public boolean canCatalogRead(String subject, CatalogType catalogType) {
        return can(subject, catalogType, "catalog", "get");
    }

    public boolean canCatalogWrite(String subject, CatalogType catalogType, String action) {
        return can(subject, catalogType, "catalog", action);
    }

    public boolean canSecurityRead(String subject, CatalogType catalogType) {
        return can(subject, catalogType, "security.roles", "get");
    }

    public boolean canSecurityWrite(String subject, CatalogType catalogType, String action) {
        return can(subject, catalogType, "security.roles", action);
    }

    private boolean can(String subject, CatalogType catalogType, String obj, String action) {
        return enforcer.enforce(subject, catalogType.toString(), obj, action);
    }

    public void reloadGroupingPolicies() {
        var groupingPolicies = enforcer.getGroupingPolicy();
        for (var policy : groupingPolicies) {
            enforcer.removeGroupingPolicy(policy);
        }

        var storedBindings = txManager.inTransactionR(tx -> roleBindingRepository.list(tx, null, null));
        for (var binding : storedBindings) {
            enforcer.addGroupingPolicy(binding.subject(), binding.role().toString(), binding.catalogType().toString());
        }
    }

    private void initSchema() {
        txManager.inTransaction(tx -> tx.createUpdate(JdbcManagementSecurityQueries.CREATE_ROLE_BINDINGS_DDL).execute());
    }

    private Enforcer createEnforcer() {
        var modelText = """
                [request_definition]
                r = sub, dom, obj, act

                [policy_definition]
                p = sub, dom, obj, act

                [role_definition]
                g = _, _, _

                [policy_effect]
                e = some(where (p.eft == allow))

                [matchers]
                m = (r.sub == "root") || (g(r.sub, p.sub, r.dom) && r.dom == p.dom && r.obj == p.obj && r.act == p.act)
                """;

        var model = new Model();
        model.loadModelFromText(modelText);
        return new Enforcer(model);
    }

    private void initRolePermissions() {
        for (var domain : allDomains()) {
            enforcer.addPolicy("catalog_admin", domain, "catalog", "create");
            enforcer.addPolicy("catalog_admin", domain, "catalog", "update");
            enforcer.addPolicy("catalog_admin", domain, "catalog", "delete");
            enforcer.addPolicy("catalog_admin", domain, "catalog", "get");

            enforcer.addPolicy("catalog_reader", domain, "catalog", "get");

            enforcer.addPolicy("security_admin", domain, "security.roles", "get");
            enforcer.addPolicy("security_admin", domain, "security.roles", "update");
            enforcer.addPolicy("security_admin", domain, "security.roles", "delete");

            enforcer.addPolicy("security_reader", domain, "security.roles", "get");
        }
    }

    private List<String> allDomains() {
        return List.of(
                CatalogType.ICEBERG.toString(),
                CatalogType.PAIMON.toString(),
                CatalogType.LANCE.toString()
        );
    }
}
