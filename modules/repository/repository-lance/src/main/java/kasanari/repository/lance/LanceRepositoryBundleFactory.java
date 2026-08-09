package kasanari.repository.lance;

import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.BackendAwareFactory;
import org.jdbi.v3.core.Handle;

public interface LanceRepositoryBundleFactory extends BackendAwareFactory {
    LanceRepositoryBundle create(String catalogName, TransactionManager<Handle> transactionManager);
}
