package kasanari.authorization.spi;

import kasanari.repository.management.common.model.CatalogType;

import java.util.List;

public interface RoleBindingAdministration {
    List<RoleBinding> list(String subject, CatalogType catalogType);

    void upsert(List<RoleBinding> bindings);

    void delete(List<RoleBinding> bindings);

    void reloadPolicies();
}
