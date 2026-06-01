package kasanari.repository.lance;

import kasanari.repository.lance.model.PagedValue;
import kasanari.repository.lance.model.TableMetadata;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TableRepository<T> {
    void upsert(T tx, String tableId, String namespacePath, String tableName, String location, Map<String, String> properties);

    boolean exists(T tx, String tableId);

    Optional<TableMetadata> get(T tx, String tableId);

    List<String> listByNamespace(T tx, String namespacePath);

    List<PagedValue<String>> listNamesByNamespacePage(T tx, String namespacePath, long cursorId, int limit);

    boolean delete(T tx, String tableId);
}
