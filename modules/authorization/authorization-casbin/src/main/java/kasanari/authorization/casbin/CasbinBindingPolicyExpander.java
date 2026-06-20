package kasanari.authorization.casbin;

import kasanari.repository.management.security.model.StoredRoleBinding;
import org.casbin.jcasbin.main.Enforcer;

import java.util.List;

final class CasbinBindingPolicyExpander {
    private CasbinBindingPolicyExpander() {
    }

    static void reloadBindingPolicies(Enforcer enforcer, List<StoredRoleBinding> bindings) {
        enforcer.clearPolicy();

        for (var binding : bindings) {
            for (var permissionPattern : CasbinPolicyBootstrap.permissionPatternsForRole(binding.role())) {
                enforcer.addPolicy(binding.subject(), binding.resource(), permissionPattern);
            }
        }
    }
}
