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
    public PaimonRepositoryBundle create(String catalogKey, KasanariDataSource dataSource) {
        var initializer = new JdbcTableInitializer(dataSource);
        return new PaimonRepositoryBundle(
                new JdbcDatabaseRepository(catalogKey),
                new JdbcTableRepository(catalogKey),
                new JdbcViewRepository(catalogKey),
                new JdbcFunctionRepository(catalogKey),
                new JdbcBranchRepository(catalogKey),
                new JdbcTagRepository(catalogKey),
                new JdbcPartitionStateRepository(catalogKey),
                KasanariCatalogLock::new,
                initializer::initialize
        );
    }
}
