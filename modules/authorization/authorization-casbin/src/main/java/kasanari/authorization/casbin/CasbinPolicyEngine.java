package kasanari.authorization.casbin;

import kasanari.repository.core.TransactionManager;
import kasanari.repository.management.security.RoleBindingRepository;
import kasanari.repository.management.security.model.StoredRoleBinding;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.jdbi.v3.core.Handle;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

final class CasbinPolicyEngine {
    private static final Logger logger = Logger.getLogger(CasbinPolicyEngine.class.getName());
    private static final long UNLOADED_REVISION = -1L;

    // TODO: add `effect` (eft) to policy definition
    private static final String MODEL_TEXT = """
            [request_definition]
            r = sub, obj, perm

            [policy_definition]
            p = sub, obj, perm
            
            [role_definition]
            g = _, _, _

            [policy_effect]
            e = some(where (p.eft == allow)) && !some(where (p.eft == deny))

            [matchers]
            m = r.sub == p.sub && resourcePrefixMatch(r.obj, p.obj) && globMatch(r.perm, p.perm)
            """;

    private final TransactionManager<Handle> txManager;
    private final RoleBindingRepository<Handle> roleBindingRepository;
    private final CasbinPolicyBootstrap policyBootstrap;
    private final AtomicReference<Enforcer> enforcer = new AtomicReference<>();
    private volatile long localRevision = UNLOADED_REVISION;

    CasbinPolicyEngine(CasbinPolicyBootstrap policyBootstrap) {
        this(null, null, policyBootstrap);
    }

    CasbinPolicyEngine(
            TransactionManager<Handle> txManager,
            RoleBindingRepository<Handle> roleBindingRepository,
            CasbinPolicyBootstrap policyBootstrap
    ) {
        this.txManager = txManager;
        this.roleBindingRepository = roleBindingRepository;
        this.policyBootstrap = policyBootstrap;
    }

    Enforcer current() {
        return enforcer.get();
    }

    void swap(Enforcer next) {
        enforcer.set(next);
    }

    Enforcer createEnforcer() {
        var model = new Model();
        model.loadModelFromText(MODEL_TEXT);
        var created = new Enforcer(model);
        created.addFunction(ResourcePrefixMatchFunction.NAME, new ResourcePrefixMatchFunction());
        return created;
    }

    Enforcer buildEnforcer(List<StoredRoleBinding> bindings) {
        var built = createEnforcer();
        if (bindings == null || bindings.isEmpty()) {
            return built;
        }

        for (var binding : bindings) {
            for (var permissionPattern : policyBootstrap.permissionPatternsForRole(binding.role())) {
                built.addPolicy(binding.subject(), binding.resource(), permissionPattern);
            }
        }
        return built;
    }

    synchronized void reloadIfChanged() {
        try {
            var dbRevision = txManager.inTransactionR(roleBindingRepository::currentRevision);
            if (localRevision == dbRevision) {
                return;
            }

            var bindings = txManager.inTransactionR(roleBindingRepository::listAll);
            swap(buildEnforcer(bindings));
            localRevision = dbRevision;
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Failed to reload Casbin policies", e);
        }
    }
}
