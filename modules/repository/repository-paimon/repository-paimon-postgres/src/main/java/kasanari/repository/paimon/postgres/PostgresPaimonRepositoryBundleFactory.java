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
