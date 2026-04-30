package kasanari.catalog.iceberg.stub;


import kasanari.catalog.iceberg.repository.TableRepository;
import kasanari.catalog.iceberg.repository.model.IcebergTableRecord;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.jdbi.v3.core.Handle;

import java.util.List;

public class JdbcTableRepositoryStub implements TableRepository {
    private final TableRepository delegate;

    public JdbcTableRepositoryStub(TableRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public IcebergTableRecord load(TableIdentifier tableIdentifier) {
        return delegate.load(tableIdentifier);
    }

    @Override
    public boolean exists(TableIdentifier tableIdentifier) {
        return delegate.exists(tableIdentifier);
    }

    @Override
    public boolean notExists(TableIdentifier identifier) {
        return delegate.notExists(identifier);
    }

    @Override
    public boolean create(TableIdentifier tableIdentifier, String newMetadataLocation) {
        return delegate.create(tableIdentifier, newMetadataLocation);
    }

    @Override
    public boolean update(TableIdentifier tableIdentifier, String previousMetadataLocation, String newMetadataLocation) {
        return delegate.update(tableIdentifier, previousMetadataLocation, newMetadataLocation);
    }

    @Override
    public boolean update(Handle tx, TableIdentifier tableIdentifier, String previousMetadataLocation, String newMetadataLocation) {
        return delegate.update(tx, tableIdentifier, previousMetadataLocation, newMetadataLocation);
    }

    @Override
    public List<TableIdentifier> findByNamespace(Namespace namespace) {
        return delegate.findByNamespace(namespace);
    }

    @Override
    public boolean delete(TableIdentifier tableIdentifier) {
        return delegate.delete(tableIdentifier);
    }

    @Override
    public boolean rename(TableIdentifier from, TableIdentifier to) {
        return delegate.rename(from, to);
    }
}
