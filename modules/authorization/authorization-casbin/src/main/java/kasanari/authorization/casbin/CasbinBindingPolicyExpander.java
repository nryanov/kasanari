package kasanari.authorization.casbin;

import kasanari.repository.management.security.model.StoredRoleBinding;
import org.casbin.jcasbin.main.Enforcer;

import java.util.List;

final class CasbinBindingPolicyExpander {
    private CasbinBindingPolicyExpander() {
    }

    static Enforcer buildEnforcer(List<StoredRoleBinding> bindings) {
        var enforcer = CasbinEnforcerFactory.createEnforcer();
        if (bindings == null || bindings.isEmpty()) {
            return enforcer;
        }

        for (var binding : bindings) {
            for (var permissionPattern : CasbinPolicyBootstrap.permissionPatternsForRole(binding.role())) {
                enforcer.addPolicy(binding.subject(), binding.resource(), permissionPattern);
            }
        }
        return enforcer;
    }
}
