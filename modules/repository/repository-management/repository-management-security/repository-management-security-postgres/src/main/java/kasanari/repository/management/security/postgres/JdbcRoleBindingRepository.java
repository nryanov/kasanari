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
                rs.getString("resource"),
                rs.getString("role")
        )).list();
    }

    @Override
    public void upsert(Handle tx, List<StoredRoleBinding> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return;
        }

        var batch = tx.prepareBatch(JdbcManagementSecurityQueries.UPSERT_ROLE_BINDING);

        for (var binding : bindings) {
            batch.bind(0, binding.subject());
            batch.bind(1, binding.resource());
            batch.bind(2, binding.role());
            batch.add();
        }

        batch.execute();
    }

    @Override
    public void delete(Handle tx, List<StoredRoleBinding> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return;
        }

        for (var binding : bindings) {
            var delete = tx.createUpdate(JdbcManagementSecurityQueries.DELETE_ROLE_BINDING);
            delete.bind(0, binding.subject());
            delete.bind(1, binding.resource());
            delete.bind(2, binding.role());
            delete.execute();
        }
    }
}
