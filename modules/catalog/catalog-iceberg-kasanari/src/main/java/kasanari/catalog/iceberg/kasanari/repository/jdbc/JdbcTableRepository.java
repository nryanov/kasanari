package kasanari.catalog.iceberg.kasanari.repository.jdbc;

import kasanari.catalog.iceberg.kasanari.repository.TableRepository;

public class JdbcTableRepository implements TableRepository {
    private final KasanariDataSource dataSource;
    private final String catalogName;

    public JdbcTableRepository(KasanariDataSource dataSource, String catalogName) {
        this.dataSource = dataSource;
        this.catalogName = catalogName;
    }
}
