package kasanari.authorization.casbin;

import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;

final class CasbinEnforcerFactory {
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

    /*
            var modelText = """
            [request_definition]
            r = sub, obj, act

            [policy_definition]
            p = sub, obj, act, eft

            [role_definition]
            g = _, _, _

            [policy_effect]
            e = some(where (p.eft == allow)) && !some(where (p.eft == deny))

            [matchers]
            m = (g(r.sub, p.sub, r.obj) && (p.obj == "*" || r.obj == p.obj) || r.sub == p.sub && r.obj == p.obj) && r.act == p.act
            """;
     */

    private CasbinEnforcerFactory() {
    }

    static Enforcer createEnforcer() {
        var model = new Model();
        model.loadModelFromText(MODEL_TEXT);
        var enforcer = new Enforcer(model);
        enforcer.addFunction(ResourcePrefixMatchFunction.NAME, new ResourcePrefixMatchFunction());
        return enforcer;
    }
}
