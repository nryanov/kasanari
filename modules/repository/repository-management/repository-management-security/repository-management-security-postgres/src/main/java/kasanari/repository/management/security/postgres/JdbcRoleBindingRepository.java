package kasanari.repository.management.security.postgres;

import kasanari.repository.management.security.RoleBindingRepository;
import kasanari.repository.management.security.model.StoredRoleBinding;
import org.jdbi.v3.core.Handle;

import java.util.List;

public class JdbcRoleBindingRepository implements RoleBindingRepository<Handle> {
    @Override
    public List<StoredRoleBinding> list(Handle tx, String subject, String resource) {
        var query = tx.createQuery(JdbcManagementSecurityQueries.SELECT_ROLE_BINDINGS);
        query.bind(0, resource);
        query.bind(1, subject);
        query.bind(2, subject);

        return query.map((rs, ctx) -> new StoredRoleBinding(
                rs.getString("subject"),
                rs.getString("resource"),
                rs.getString("role")
        )).list();
    }

    @Override
    public List<StoredRoleBinding> listAll(Handle tx) {
        return tx.createQuery(JdbcManagementSecurityQueries.SELECT_ALL_ROLE_BINDINGS)
                .map((rs, ctx) -> new StoredRoleBinding(
                        rs.getString("subject"),
                        rs.getString("resource"),
                        rs.getString("role")
                )).list();
    }

    @Override
    public void add(Handle tx, List<StoredRoleBinding> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return;
        }

        for (var binding : bindings) {
            var insert = tx.createUpdate(JdbcManagementSecurityQueries.INSERT_ROLE_BINDING);
            insert.bind(0, binding.subject());
            insert.bind(1, binding.resource());
            insert.bind(2, binding.role());
            insert.execute();
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
            delete.bind(1, binding.resource());
            delete.bind(2, binding.role());
            delete.execute();
        }
    }
}
