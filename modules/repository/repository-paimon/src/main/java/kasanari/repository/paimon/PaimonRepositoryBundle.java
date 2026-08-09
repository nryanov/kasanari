package kasanari.repository.paimon;

import org.apache.paimon.catalog.CatalogLock;
import org.jdbi.v3.core.Handle;

import java.util.function.Function;

public record PaimonRepositoryBundle(
        DatabaseRepository<Handle> databaseRepository,
        TableRepository<Handle> tableRepository,
        ViewRepository<Handle> viewRepository,
        FunctionRepository<Handle> functionRepository,
        BranchRepository<Handle> branchRepository,
        TagRepository<Handle> tagRepository,
        PartitionStateRepository<Handle> partitionStateRepository,
        Function<Handle, CatalogLock> catalogLockFactory,
        Runnable schemaInitializer
) {
}
