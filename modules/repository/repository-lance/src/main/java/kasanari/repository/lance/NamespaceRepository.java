package kasanari.repository.lance;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface NamespaceRepository<T> {
    void upsert(T tx, String namespacePath, Map<String, String> properties);

    boolean exists(T tx, String namespacePath);

    Optional<Map<String, String>> properties(T tx, String namespacePath);

    List<String> list(T tx, String parentPath);

    void delete(T tx, String namespacePath) ;
}
