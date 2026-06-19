package kasanari.repository.management.security.postgres;

import kasanari.repository.management.security.RoleBindingRepository;
import kasanari.repository.management.security.model.StoredRoleBinding;
import org.jdbi.v3.core.Handle;

import java.util.List;

public class JdbcRoleBindingRepository implements RoleBindingRepository<Handle> {
    @Override
    public List<StoredRoleBinding> list(Handle tx, String subject, String resourcePrefix) {
        var query = tx.createQuery(JdbcManagementSecurityQueries.SELECT_ROLE_BINDINGS);
        query.bind(0, subject);
        query.bind(1, subject);
        query.bind(2, resourcePrefix);
        query.bind(3, resourcePrefix == null ? null : resourcePrefix + "%");

        return query.map((rs, ctx) -> new StoredRoleBinding(
                rs.getString("subject"),
                rs.getString("role_name"),
                rs.getString("resource")
        )).list();
    }

    @Override
    public void upsert(Handle tx, List<StoredRoleBinding> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return;
        }

        for (var binding : bindings) {
            var upsert = tx.createUpdate(JdbcManagementSecurityQueries.UPSERT_ROLE_BINDING);
            upsert.bind(0, binding.subject());
            upsert.bind(1, binding.role());
            upsert.bind(2, binding.resource());
            upsert.execute();
        }
    }

    @Override
    public void delete(Handle tx, List<StoredRoleBinding> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return;
        }

        for (var binding : bindings) {
            var delete = tx.createUpdate(JdbcManagementSecurityQueries.DELETE_ROLE_BINDING);
            delete.bind(0, binding.subject());
            delete.bind(1, binding.role());
            delete.bind(2, binding.resource());
            delete.execute();
        }
    }
}
