package kasanari.repository.lance;

import kasanari.repository.lance.model.TableRow;

import java.util.List;
import java.util.Map;

public interface TableRepository<T> {
    void upsert(T tx, String tableId, String namespacePath, String tableName, String location, Map<String, String> properties, boolean declaredOnly);

    boolean exists(T tx, String tableId);

    TableRow get(T tx, String tableId);

    List<String> listByNamespace(T tx, String namespacePath);

    void delete(T tx, String tableId);
}
