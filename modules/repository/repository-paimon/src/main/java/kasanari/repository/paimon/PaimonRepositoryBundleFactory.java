package kasanari.repository.paimon;

import kasanari.repository.jdbc.BackendAwareFactory;
import kasanari.repository.jdbc.KasanariDataSource;

public interface PaimonRepositoryBundleFactory extends BackendAwareFactory {
    PaimonRepositoryBundle create(String catalogKey, KasanariDataSource dataSource);
}
