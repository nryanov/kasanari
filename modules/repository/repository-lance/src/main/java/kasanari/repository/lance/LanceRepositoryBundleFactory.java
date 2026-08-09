package kasanari.repository.lance;

import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.BackendAwareFactory;
import org.jdbi.v3.core.Handle;

public interface LanceRepositoryBundleFactory extends BackendAwareFactory {
    /**
     * @param catalogName management catalog id used to isolate INTERNAL rows
     */
    LanceRepositoryBundle create(String catalogName, TransactionManager<Handle> transactionManager);
}
