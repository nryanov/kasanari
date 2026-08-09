package kasanari.repository.iceberg.yugabyte;

import kasanari.repository.iceberg.IcebergRepositoryBundle;
import kasanari.repository.iceberg.IcebergRepositoryBundleFactory;
import kasanari.repository.jdbc.KasanariDataSource;
import kasanari.repository.jdbc.RepositoryBackend;

public final class YugabyteIcebergRepositoryBundleFactory implements IcebergRepositoryBundleFactory {
    @Override
    public RepositoryBackend backend() {
        return RepositoryBackend.YUGABYTE;
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
