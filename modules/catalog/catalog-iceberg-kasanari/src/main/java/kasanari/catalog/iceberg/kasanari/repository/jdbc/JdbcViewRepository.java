package kasanari.catalog.iceberg.kasanari.repository.jdbc;

import kasanari.catalog.iceberg.kasanari.repository.ViewRepository;

public class JdbcViewRepository implements ViewRepository {
    private final KasanariDataSource dataSource;
    private final String catalogName;

    public JdbcViewRepository(KasanariDataSource dataSource, String catalogName) {
        this.dataSource = dataSource;
        this.catalogName = catalogName;
    }
}
