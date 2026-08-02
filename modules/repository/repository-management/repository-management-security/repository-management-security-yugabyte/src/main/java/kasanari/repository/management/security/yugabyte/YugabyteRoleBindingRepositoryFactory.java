package kasanari.repository.management.security.yugabyte;

import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.RepositoryBackend;
import kasanari.repository.management.security.RoleBindingRepository;
import kasanari.repository.management.security.RoleBindingRepositoryFactory;
import org.jdbi.v3.core.Handle;

public final class YugabyteRoleBindingRepositoryFactory implements RoleBindingRepositoryFactory {
    @Override
    public RepositoryBackend backend() {
        return RepositoryBackend.YUGABYTE;
    }

    @Override
    public RoleBindingRepository<Handle> createRepository() {
        return new JdbcRoleBindingRepository();
    }

    @Override
    public void initSchema(TransactionManager<Handle> txManager) {
        txManager.inTransaction(tx -> {
            tx.createUpdate(JdbcManagementSecurityQueries.CREATE_ROLE_BINDINGS_DDL).execute();
            tx.createUpdate(JdbcManagementSecurityQueries.CREATE_ROLE_BINDING_REVISION_DDL).execute();
            tx.createUpdate(JdbcManagementSecurityQueries.INSERT_ROLE_BINDING_REVISION).execute();
        });
    }
}
