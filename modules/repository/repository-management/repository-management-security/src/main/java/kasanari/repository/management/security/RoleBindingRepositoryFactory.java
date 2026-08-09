package kasanari.repository.management.security;

import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.BackendAwareFactory;
import org.jdbi.v3.core.Handle;

public interface RoleBindingRepositoryFactory extends BackendAwareFactory {
    RoleBindingRepository<Handle> createRepository();

    void initSchema(TransactionManager<Handle> txManager);
}
