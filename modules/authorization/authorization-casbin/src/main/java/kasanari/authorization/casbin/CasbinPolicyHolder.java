package kasanari.authorization.casbin;

import org.casbin.jcasbin.main.Enforcer;

import java.util.concurrent.atomic.AtomicReference;

final class CasbinPolicyHolder {
    private final AtomicReference<Enforcer> enforcer = new AtomicReference<>();

    Enforcer current() {
        return enforcer.get();
    }

    void swap(Enforcer next) {
        enforcer.set(next);
    }
}
