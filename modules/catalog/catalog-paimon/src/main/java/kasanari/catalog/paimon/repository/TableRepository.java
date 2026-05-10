package kasanari.catalog.paimon.repository;

import kasanari.catalog.paimon.model.TableRecord;
import org.apache.paimon.catalog.Identifier;

import java.util.List;
import java.util.Optional;

public interface TableRepository<T> {
    List<TableRecord> findAll(T tx, String database);

    List<TableRecord> findPage(T tx, String database, String tableNamePatternLike, long idAfter, int pageSize);

    List<TableRecord> findPageGlobally(T tx, String databaseNamePatternLike, String tableNamePatternLike, long idAfter, int pageSize);

    boolean delete(T tx, Identifier identifier);

    void create(T tx, TableRecord record);

    void alter(T tx, TableRecord record);

    void rename(T tx, Identifier fromTable, Identifier toTable);

    boolean exists(T tx, Identifier table);

    Optional<TableRecord> find(T tx, Identifier table);

    Optional<TableRecord> findByUuid(T tx, String tableUuid);
}
