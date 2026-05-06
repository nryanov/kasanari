package kasanari.catalog.paimon.repository;

import kasanari.catalog.paimon.model.FunctionRecord;
import org.apache.paimon.catalog.Identifier;

import java.util.List;
import java.util.Optional;

public interface FunctionRepository<T> {
    List<FunctionRecord> findAll(T tx, String database);

    Optional<FunctionRecord> find(T tx, Identifier function);

    boolean delete(T tx, Identifier identifier);

    void create(T tx, FunctionRecord record);

    void alter(T tx, FunctionRecord record);
}
