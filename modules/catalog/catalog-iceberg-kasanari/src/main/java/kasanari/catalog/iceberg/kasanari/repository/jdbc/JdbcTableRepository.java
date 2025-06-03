package kasanari.catalog.iceberg.kasanari.repository.jdbc;

import kasanari.catalog.iceberg.kasanari.repository.TableRepository;
import kasanari.catalog.iceberg.kasanari.repository.model.IcebergTableRecord;
import kasanari.catalog.iceberg.kasanari.utils.IcebergUtils;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.jdbi.v3.core.Handle;

import java.util.ArrayList;
import java.util.List;

public class JdbcTableRepository implements TableRepository {
    private final KasanariDataSource dataSource;
    private final String catalogName;

    public JdbcTableRepository(KasanariDataSource dataSource, String catalogName) {
        this.dataSource = dataSource;
        this.catalogName = catalogName;
    }

    @Override
    public IcebergTableRecord load(TableIdentifier tableIdentifier) {
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
        var namespaceName = IcebergUtils.namespaceName(tableIdentifier.namespace());

        return dataSource.getJdbi().inTransaction(tx -> {
            var query = tx.createUpdate(JdbcQueries.CREATE_TABLE);
            query.bind(0, catalogName);
            query.bind(1, namespaceName);
            query.bind(2, tableIdentifier.name());
            query.bind(3, newMetadataLocation);

            var affectedRows = query.execute();

            return affectedRows == 1;
        });
    }

    @Override
    public boolean update(TableIdentifier tableIdentifier, String previousMetadataLocation, String newMetadataLocation) {
        return dataSource.getJdbi().inTransaction(tx -> update0(tx, tableIdentifier, previousMetadataLocation, newMetadataLocation));
    }

    @Override
    public boolean update(Handle tx, TableIdentifier tableIdentifier, String previousMetadataLocation, String newMetadataLocation) {
        return update0(tx, tableIdentifier, previousMetadataLocation, newMetadataLocation);
    }

    private boolean update0(Handle tx, TableIdentifier tableIdentifier, String previousMetadataLocation, String newMetadataLocation) {
        var namespaceName = IcebergUtils.namespaceName(tableIdentifier.namespace());

        var query = tx.createUpdate(JdbcQueries.UPDATE_TABLE);
        query.bind(0, newMetadataLocation);
        query.bind(1, previousMetadataLocation);
        query.bind(2, catalogName);
        query.bind(3, namespaceName);
        query.bind(4, tableIdentifier.name());
        query.bind(5, previousMetadataLocation);

        var affectedRows = query.execute();

        return affectedRows == 1;
    }

    @Override
    public List<TableIdentifier> findByNamespace(Namespace namespace) {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        return dataSource.getJdbi().inTransaction(tx -> {
            var result = new ArrayList<TableIdentifier>();

            var query = tx.createQuery(JdbcQueries.LIST_TABLES);
            query.bind(0, catalogName);
            query.bind(1, namespaceName);

            query.mapTo(String.class).forEach(it -> result.add(TableIdentifier.of(namespace, it)));

            return result;
        });
    }

    @Override
    public boolean delete(TableIdentifier tableIdentifier) {
        var namespaceName = IcebergUtils.namespaceName(tableIdentifier.namespace());

        return dataSource.getJdbi().inTransaction(tx -> {
            var query = tx.createUpdate(JdbcQueries.DELETE_TABLE);
            query.bind(0, catalogName);
            query.bind(1, namespaceName);
            query.bind(2, tableIdentifier.name());

            var affectedRows = query.execute();

            return affectedRows == 1;
        });
    }

    @Override
    public boolean rename(TableIdentifier from, TableIdentifier to) {
        var namespaceNameFrom = IcebergUtils.namespaceName(from.namespace());
        var namespaceNameTo = IcebergUtils.namespaceName(to.namespace());

        return dataSource.getJdbi().inTransaction(tx -> {
            var query = tx.createUpdate(JdbcQueries.RENAME_TABLE);
            query.bind(0, namespaceNameTo);
            query.bind(1, to.name());
            query.bind(2, catalogName);
            query.bind(3, namespaceNameFrom);
            query.bind(4, from.name());

            var affectedRows = query.execute();

            return affectedRows == 1;
        });
    }
}
