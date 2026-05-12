package kasanari.repository.paimon;

import kasanari.repository.paimon.model.DatabaseRecord;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface DatabaseRepository<T> {
    Optional<DatabaseRecord> findByName(T tx, String name);

    void create(T tx, DatabaseRecord record);

    boolean delete(T tx, String name);

    boolean alter(T tx, String name, Map<String, String> update, Set<String> remove);

    List<DatabaseRecord> findAll(T tx);
}
