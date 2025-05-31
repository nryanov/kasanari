package kasanari.catalog.iceberg.kasanari.repository.jdbc;

import kasanari.catalog.iceberg.kasanari.repository.TableRepository;
import kasanari.catalog.iceberg.kasanari.repository.model.IcebergTableRecord;
import kasanari.catalog.iceberg.kasanari.utils.IcebergUtils;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.NoSuchTableException;

public class JdbcTableRepository implements TableRepository {
    private final KasanariDataSource dataSource;
    private final String catalogName;

    public JdbcTableRepository(KasanariDataSource dataSource, String catalogName) {
        this.dataSource = dataSource;
        this.catalogName = catalogName;
    }

    @Override
    public IcebergTableRecord loadTable(TableIdentifier tableIdentifier) {
        return dataSource.getJdbi().inTransaction(tx -> {
            var namespaceName = IcebergUtils.namespaceName(tableIdentifier.namespace());

            var query = tx.createQuery(JdbcQueries.SELECT_TABLE);
            query.bind(0, catalogName);
            query.bind(1, namespaceName);
            query.bind(2, tableIdentifier.name());

            var result = query.map((rs, ctx) -> new IcebergTableRecord(
                    rs.getString("catalog_name"),
                    rs.getString("namespace_name"),
                    rs.getString("table_name"),
                    rs.getString("metadata_location"),
                    rs.getString("previous_metadata_location")
            ));

            var maybeTable = result.findFirst();

            if (maybeTable.isEmpty()) {
                throw new NoSuchTableException(String.format("Table `%s` does not exist in namespace `%s` and catalog `%s`",
                        tableIdentifier.name(), namespaceName, catalogName));
            }

            return maybeTable.get();
        });
    }

    @Override
    public boolean exists(TableIdentifier tableIdentifier) {
        var namespaceName = IcebergUtils.namespaceName(tableIdentifier.namespace());

        return dataSource.getJdbi().inTransaction(tx -> {
            var query = tx.createQuery(JdbcQueries.CHECK_IF_TABLE_EXISTS);
            query.bind(0, catalogName);
            query.bind(1, namespaceName);
            query.bind(2, tableIdentifier.name());

            return query.mapTo(Boolean.class).first();
        });
    }

    @Override
    public boolean create(TableIdentifier tableIdentifier, String newMetadataLocation) {
        return false;
    }

    @Override
    public boolean update(TableIdentifier tableIdentifier, String previousMetadataLocation, String newMetadataLocation) {
        return false;
    }
}
