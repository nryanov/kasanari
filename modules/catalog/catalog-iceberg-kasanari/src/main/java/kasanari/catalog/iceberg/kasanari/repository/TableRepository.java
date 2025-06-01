package kasanari.catalog.iceberg.kasanari.repository;

import kasanari.catalog.iceberg.kasanari.repository.model.IcebergTableRecord;
import org.apache.iceberg.catalog.Namespace;
import org.apache.iceberg.catalog.TableIdentifier;

import java.util.List;

public interface TableRepository {
    IcebergTableRecord load(TableIdentifier tableIdentifier);

    boolean exists(TableIdentifier tableIdentifier);

    boolean create(TableIdentifier tableIdentifier, String newMetadataLocation);

    boolean update(TableIdentifier tableIdentifier, String previousMetadataLocation, String newMetadataLocation);

    List<TableIdentifier> findByNamespace(Namespace namespace);

    boolean delete(TableIdentifier tableIdentifier);
}
