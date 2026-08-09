package kasanari.repository.paimon.postgres;

import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.repository.jdbc.RepositoryBackend;
import kasanari.repository.paimon.PaimonRepositoryBundle;
import kasanari.repository.paimon.PaimonRepositoryBundleFactory;

public final class PostgresPaimonRepositoryBundleFactory implements PaimonRepositoryBundleFactory {
    @Override
    public RepositoryBackend backend() {
        return RepositoryBackend.POSTGRES;
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
