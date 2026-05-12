package kasanari.repository.management;

import kasanari.catalog.management.model.CatalogType;
import kasanari.repository.management.model.StoredRoleBinding;

import java.util.List;

public interface RoleBindingRepository<T> {
    List<StoredRoleBinding> list(T tx, String subject, CatalogType catalogType);

    void upsert(T tx, List<StoredRoleBinding> bindings);

    void delete(T tx, List<StoredRoleBinding> bindings);
}
