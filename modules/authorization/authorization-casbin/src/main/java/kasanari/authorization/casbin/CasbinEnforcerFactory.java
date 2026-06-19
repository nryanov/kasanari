package kasanari.authorization.casbin;

import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;

final class CasbinEnforcerFactory {
    private static final String MODEL_TEXT = """
            [request_definition]
            r = sub, obj, perm

            [policy_definition]
            p = sub, obj, perm

            [policy_effect]
            e = some(where (p.eft == allow))

            [matchers]
            m = r.sub == p.sub && keyMatch3(r.obj, p.obj) && globMatch(r.perm, p.perm)
            """;

    private CasbinEnforcerFactory() {
    }

    static Enforcer createEnforcer() {
        var model = new Model();
        model.loadModelFromText(MODEL_TEXT);
        return new Enforcer(model);
    }
}
