package kasanari.repository.lance;

import org.jdbi.v3.core.Handle;

public record LanceRepositoryBundle(
        NamespaceRepository<Handle> namespaceRepository,
        TableRepository<Handle> tableRepository,
        Runnable schemaInitializer
) {
}
