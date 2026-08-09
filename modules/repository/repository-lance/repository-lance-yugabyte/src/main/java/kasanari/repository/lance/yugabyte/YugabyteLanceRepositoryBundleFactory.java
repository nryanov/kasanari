package kasanari.repository.lance.yugabyte;

import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.RepositoryBackend;
import kasanari.repository.lance.LanceRepositoryBundle;
import kasanari.repository.lance.LanceRepositoryBundleFactory;
import org.jdbi.v3.core.Handle;

public final class YugabyteLanceRepositoryBundleFactory implements LanceRepositoryBundleFactory {
    @Override
    public RepositoryBackend backend() {
        return RepositoryBackend.YUGABYTE;
    }

    @Override
    public LanceRepositoryBundle create(String catalogKey, TransactionManager<Handle> transactionManager) {
        if (catalogKey == null || catalogKey.isBlank()) {
            throw new IllegalArgumentException("catalogKey is required for the Yugabyte Lance repository backend");
        }
        var initializer = new JdbcTableInitializer(transactionManager);
        return new LanceRepositoryBundle(
                new JdbcNamespaceRepository(catalogKey),
                new JdbcTableRepository(catalogKey),
                initializer::initialize
        );
    }
}
