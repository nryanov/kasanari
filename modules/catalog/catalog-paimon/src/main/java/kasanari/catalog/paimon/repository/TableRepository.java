package kasanari.catalog.paimon.repository;

import kasanari.catalog.paimon.model.TableRecord;
import org.apache.paimon.catalog.Identifier;

import java.util.List;

public interface TableRepository<T> {
    List<TableRecord> findAll(T tx, String database);

    boolean delete(T tx, Identifier identifier);

    void create(T tx, TableRecord record);

    void alter(T tx, TableRecord record);

    void rename(T tx, Identifier fromTable, Identifier toTable);
}
