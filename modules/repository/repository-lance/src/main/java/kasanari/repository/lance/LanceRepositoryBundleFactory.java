package kasanari.repository.lance;

import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.BackendAwareFactory;
import org.jdbi.v3.core.Handle;

public interface LanceRepositoryBundleFactory extends BackendAwareFactory {
    /**
     * @param catalogKey management catalog id; required for Yugabyte, ignored by Postgres impl
     */
    LanceRepositoryBundle create(String catalogKey, TransactionManager<Handle> transactionManager);
}
