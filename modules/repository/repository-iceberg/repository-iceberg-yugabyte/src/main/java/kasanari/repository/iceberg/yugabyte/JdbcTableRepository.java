package kasanari.repository.iceberg.yugabyte;

import kasanari.repository.iceberg.TableRepository;
import kasanari.repository.iceberg.model.IcebergTableRecord;
import kasanari.repository.iceberg.IcebergUtils;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.exceptions.NoSuchTableException;
import org.jdbi.v3.core.Handle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTableRepository implements TableRepository<Handle> {
    private final String catalogName;

    public JdbcTableRepository(String catalogName) {
        this.catalogName = catalogName;
    }

    @Override
    public IcebergTableRecord findUnsafe(Handle tx, TableIdentifier tableIdentifier) {
        var namespaceName = IcebergUtils.namespaceName(tableIdentifier.namespace());
        return find(tx, tableIdentifier).orElseThrow(() -> new NoSuchTableException(
                String.format("Table `%s` does not exist in namespace `%s` and catalog `%s`",
                        tableIdentifier.name(), namespaceName, catalogName)));
    }

    @Override
    public Optional<IcebergTableRecord> find(Handle tx, TableIdentifier tableIdentifier) {
        var namespaceName = IcebergUtils.namespaceName(tableIdentifier.namespace());

        var query = tx.createQuery(JdbcQueries.SELECT_TABLE);
        query.bind(0, catalogName);
        query.bind(1, namespaceName);
        query.bind(2, tableIdentifier.name());

        return query.map((rs, ctx) -> new IcebergTableRecord(
                rs.getString("catalog_name"),
                rs.getString("namespace_name"),
                rs.getString("table_name"),
                rs.getString("metadata_location"),
                rs.getString("previous_metadata_location")
        )).findFirst();
    }

    @Override
    public boolean exists(Handle tx,TableIdentifier tableIdentifier) {
        var namespaceName = IcebergUtils.namespaceName(tableIdentifier.namespace());

        var query = tx.createQuery(JdbcQueries.CHECK_IF_TABLE_EXISTS);
        query.bind(0, catalogName);
        query.bind(1, namespaceName);
        query.bind(2, tableIdentifier.name());

        return query.mapTo(Boolean.class).first();
    }

    @Override
    public boolean create(Handle tx,TableIdentifier tableIdentifier, String newMetadataLocation) {
        var namespaceName = IcebergUtils.namespaceName(tableIdentifier.namespace());

        var query = tx.createUpdate(JdbcQueries.CREATE_TABLE);
        query.bind(0, catalogName);
        query.bind(1, namespaceName);
        query.bind(2, tableIdentifier.name());
        query.bind(3, newMetadataLocation);

        var affectedRows = query.execute();

        return affectedRows == 1;
    }

    @Override
    public boolean update(Handle tx,TableIdentifier tableIdentifier, String previousMetadataLocation, String newMetadataLocation) {
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
    public List<TableIdentifier> findByNamespace(Handle tx,Namespace namespace) {
        var namespaceName = IcebergUtils.namespaceName(namespace);

        var result = new ArrayList<TableIdentifier>();

        var query = tx.createQuery(JdbcQueries.LIST_TABLES);
        query.bind(0, catalogName);
        query.bind(1, namespaceName);

        query.mapTo(String.class).forEach(it -> result.add(TableIdentifier.of(namespace, it)));

        return result;
    }

    @Override
    public boolean delete(Handle tx,TableIdentifier tableIdentifier) {
        var namespaceName = IcebergUtils.namespaceName(tableIdentifier.namespace());

        var query = tx.createUpdate(JdbcQueries.DELETE_TABLE);
        query.bind(0, catalogName);
        query.bind(1, namespaceName);
        query.bind(2, tableIdentifier.name());

        var affectedRows = query.execute();

        return affectedRows == 1;
    }

    @Override
    public boolean rename(Handle tx,TableIdentifier from, TableIdentifier to) {
        var namespaceNameFrom = IcebergUtils.namespaceName(from.namespace());
        var namespaceNameTo = IcebergUtils.namespaceName(to.namespace());

        var query = tx.createUpdate(JdbcQueries.RENAME_TABLE);
        query.bind(0, namespaceNameTo);
        query.bind(1, to.name());
        query.bind(2, catalogName);
        query.bind(3, namespaceNameFrom);
        query.bind(4, from.name());

        var affectedRows = query.execute();

        return affectedRows == 1;
    }
}
