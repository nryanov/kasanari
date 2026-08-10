package kasanari.repository.iceberg.postgres;

import kasanari.repository.iceberg.IcebergRepositoryBundle;
import kasanari.repository.iceberg.IcebergRepositoryBundleFactory;
import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.repository.jdbc.RepositoryBackend;

public final class PostgresIcebergRepositoryBundleFactory implements IcebergRepositoryBundleFactory {
    @Override
    public RepositoryBackend backend() {
        return RepositoryBackend.POSTGRES;
    }

    @Override
    public IcebergRepositoryBundle create(String catalogName, KasanariDataSource dataSource) {
        var initializer = new JdbcTableInitializer(dataSource);
        return new IcebergRepositoryBundle(
                new JdbcCatalogRepository(catalogName),
                new JdbcNamespaceRepository(catalogName),
                new JdbcTableRepository(catalogName),
                new JdbcViewRepository(catalogName),
                initializer::initialize
        );
    }
}
