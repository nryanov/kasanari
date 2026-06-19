package kasanari.authorization.spi;

import java.util.List;

public interface RoleBindingAdministration {
    List<RoleBinding> list(String subject, String resourcePrefix);

    void upsert(List<RoleBinding> bindings);

    void delete(List<RoleBinding> bindings);

    void reloadPolicies();
}
