package kasanari.catalog.iceberg.kasanari.repository;

import kasanari.catalog.iceberg.kasanari.repository.model.IcebergViewRecord;
import org.apache.iceberg.catalog.TableIdentifier;

public interface ViewRepository {
    IcebergViewRecord loadView(TableIdentifier tableIdentifier);

    boolean exists(TableIdentifier tableIdentifier);

    boolean create(TableIdentifier tableIdentifier, String newMetadataLocation);

    boolean update(TableIdentifier tableIdentifier, String previousMetadataLocation, String newMetadataLocation);
}
