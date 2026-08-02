package kasanari.repository.iceberg;

import org.jdbi.v3.core.Handle;

public record IcebergRepositoryBundle(
        CatalogRepository<Handle> catalogRepository,
        NamespaceRepository<Handle> namespaceRepository,
        TableRepository<Handle> tableRepository,
        ViewRepository<Handle> viewRepository,
        Runnable schemaInitializer
) {
}
