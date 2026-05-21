package kasanari.repository.management.security;

import kasanari.core.model.CatalogType;
import kasanari.repository.management.security.model.StoredRoleBinding;

import java.util.List;

public interface RoleBindingRepository<T> {
    List<StoredRoleBinding> list(T tx, String subject, CatalogType catalogType);

    void upsert(T tx, List<StoredRoleBinding> bindings);

    void delete(T tx, List<StoredRoleBinding> bindings);
}
