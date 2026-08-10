package kasanari.repository.paimon.yugabyte;

import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.repository.jdbc.RepositoryBackend;
import kasanari.repository.paimon.PaimonRepositoryBundle;
import kasanari.repository.paimon.PaimonRepositoryBundleFactory;

public final class YugabytePaimonRepositoryBundleFactory implements PaimonRepositoryBundleFactory {
    @Override
    public RepositoryBackend backend() {
        return RepositoryBackend.YUGABYTE;
    }

    @Override
    public PaimonRepositoryBundle create(String catalogName, KasanariDataSource dataSource) {
        var initializer = new JdbcTableInitializer(dataSource);
        return new PaimonRepositoryBundle(
                new JdbcDatabaseRepository(catalogName),
                new JdbcTableRepository(catalogName),
                new JdbcViewRepository(catalogName),
                new JdbcFunctionRepository(catalogName),
                new JdbcBranchRepository(catalogName),
                new JdbcTagRepository(catalogName),
                new JdbcPartitionStateRepository(catalogName),
                KasanariCatalogLock::new,
                initializer::initialize
        );
    }
}
