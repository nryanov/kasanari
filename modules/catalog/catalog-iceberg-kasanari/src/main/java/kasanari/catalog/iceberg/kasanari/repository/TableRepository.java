package kasanari.catalog.iceberg.kasanari.repository;

import kasanari.catalog.iceberg.kasanari.repository.model.IcebergTableRecord;
import org.apache.iceberg.catalog.TableIdentifier;

public interface TableRepository {
    IcebergTableRecord loadTable(TableIdentifier tableIdentifier);

    boolean exists(TableIdentifier tableIdentifier);

    boolean create(TableIdentifier tableIdentifier, String newMetadataLocation);

    boolean update(TableIdentifier tableIdentifier, String previousMetadataLocation, String newMetadataLocation);
}
