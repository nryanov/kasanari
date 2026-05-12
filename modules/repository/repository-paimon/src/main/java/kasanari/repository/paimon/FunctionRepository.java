package kasanari.repository.paimon;

import kasanari.repository.paimon.model.FunctionRecord;
import org.apache.paimon.catalog.Identifier;

import java.util.List;
import java.util.Optional;

public interface FunctionRepository<T> {
    List<FunctionRecord> findAll(T tx, String database);

    List<FunctionRecord> findPage(T tx, String database, String functionNamePatternLike, long idAfter, int pageSize);

    List<FunctionRecord> findPageGlobally(T tx, String databaseNamePatternLike, String functionNamePatternLike, long idAfter, int pageSize);

    Optional<FunctionRecord> find(T tx, Identifier function);

    boolean delete(T tx, Identifier identifier);

    void create(T tx, FunctionRecord record);

    void alter(T tx, FunctionRecord record);
}
