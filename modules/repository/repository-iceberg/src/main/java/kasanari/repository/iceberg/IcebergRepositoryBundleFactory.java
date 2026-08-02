package kasanari.repository.iceberg;

import kasanari.repository.jdbc.BackendAwareFactory;
import kasanari.repository.jdbc.KasanariDataSource;

public interface IcebergRepositoryBundleFactory extends BackendAwareFactory {
    IcebergRepositoryBundle create(String catalogKey, KasanariDataSource dataSource);
}
