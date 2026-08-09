package kasanari.repository.lance.postgres;

import kasanari.repository.core.TransactionManager;
import kasanari.repository.jdbc.RepositoryBackend;
import kasanari.repository.lance.LanceRepositoryBundle;
import kasanari.repository.lance.LanceRepositoryBundleFactory;
import org.jdbi.v3.core.Handle;

public final class PostgresLanceRepositoryBundleFactory implements LanceRepositoryBundleFactory {
    @Override
    public RepositoryBackend backend() {
        return RepositoryBackend.POSTGRES;
    }

    @Override
    public LanceRepositoryBundle create(String catalogKey, TransactionManager<Handle> transactionManager) {
        var initializer = new JdbcTableInitializer(transactionManager);
        return new LanceRepositoryBundle(
                new JdbcNamespaceRepository(),
                new JdbcTableRepository(),
                initializer::initialize
        );
    }
}
