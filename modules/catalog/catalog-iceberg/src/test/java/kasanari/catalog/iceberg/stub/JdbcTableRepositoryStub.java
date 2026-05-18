package kasanari.catalog.iceberg.stub;


import kasanari.repository.iceberg.TableRepository;
import kasanari.repository.iceberg.model.IcebergTableRecord;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;
import org.jdbi.v3.core.Handle;

import java.util.List;
import java.util.Optional;

public class JdbcTableRepositoryStub implements TableRepository<Handle> {
    private final TableRepository<Handle> delegate;

    public JdbcTableRepositoryStub(TableRepository<Handle> delegate) {
        this.delegate = delegate;
    }

    @Override
    public IcebergTableRecord findUnsafe(Handle tx, TableIdentifier tableIdentifier) {
        return delegate.findUnsafe(tx, tableIdentifier);
    }

    @Override
    public Optional<IcebergTableRecord> find(Handle tx, TableIdentifier tableIdentifier) {
        return delegate.find(tx, tableIdentifier);
    }

    @Override
    public boolean exists(Handle tx, TableIdentifier tableIdentifier) {
        return delegate.exists(tx, tableIdentifier);
    }

    @Override
    public boolean notExists(Handle tx, TableIdentifier identifier) {
        return delegate.notExists(tx, identifier);
    }

    @Override
    public boolean create(Handle tx, TableIdentifier tableIdentifier, String newMetadataLocation) {
        return delegate.create(tx, tableIdentifier, newMetadataLocation);
    }

    @Override
    public boolean update(Handle tx, TableIdentifier tableIdentifier, String previousMetadataLocation, String newMetadataLocation) {
        return delegate.update(tx, tableIdentifier, previousMetadataLocation, newMetadataLocation);
    }

    @Override
    public List<TableIdentifier> findByNamespace(Handle tx, Namespace namespace) {
        return delegate.findByNamespace(tx, namespace);
    }

    @Override
    public boolean delete(Handle tx, TableIdentifier tableIdentifier) {
        return delegate.delete(tx, tableIdentifier);
    }

    @Override
    public boolean rename(Handle tx, TableIdentifier from, TableIdentifier to) {
        return delegate.rename(tx, from, to);
    }
}
