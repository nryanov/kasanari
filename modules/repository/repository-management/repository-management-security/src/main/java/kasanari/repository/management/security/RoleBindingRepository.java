package kasanari.repository.management.security;

import kasanari.repository.management.security.model.StoredRoleBinding;

import java.util.List;

public interface RoleBindingRepository<T> {
    List<StoredRoleBinding> list(T tx, String subject, String resource);

    List<StoredRoleBinding> listAll(T tx);

    void add(T tx, List<StoredRoleBinding> bindings);

    void delete(T tx, List<StoredRoleBinding> bindings);
}
