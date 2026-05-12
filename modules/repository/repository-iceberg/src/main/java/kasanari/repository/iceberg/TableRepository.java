package kasanari.repository.iceberg;

import kasanari.repository.iceberg.model.IcebergTableRecord;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;

import java.util.List;

public interface TableRepository<T> {
    IcebergTableRecord load(T tx, TableIdentifier tableIdentifier);

    boolean exists(T tx, TableIdentifier tableIdentifier);

    default boolean notExists(T tx, TableIdentifier identifier) {
        return !exists(tx, identifier);
    }

    boolean create(T tx, TableIdentifier tableIdentifier, String newMetadataLocation);

    boolean update(T tx, TableIdentifier tableIdentifier, String previousMetadataLocation, String newMetadataLocation);

    List<TableIdentifier> findByNamespace(T tx, Namespace namespace);

    boolean delete(T tx, TableIdentifier tableIdentifier);

    boolean rename(T tx, TableIdentifier from, TableIdentifier to);
}
