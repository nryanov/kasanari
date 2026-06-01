package kasanari.repository.lance;

import kasanari.repository.lance.model.PagedValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface NamespaceRepository<T> {
    void upsert(T tx, String namespacePath, Map<String, String> properties);

    boolean exists(T tx, String namespacePath);

    Optional<Map<String, String>> properties(T tx, String namespacePath);

    List<String> list(T tx, String parentPath);

    List<PagedValue<String>> listPage(T tx, String parentPath, long cursorId, int limit);

    void delete(T tx, String namespacePath) ;
}
