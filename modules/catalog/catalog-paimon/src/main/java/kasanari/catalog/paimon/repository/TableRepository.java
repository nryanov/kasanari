package kasanari.catalog.paimon.repository;

import kasanari.catalog.paimon.model.TableRecord;
import org.apache.paimon.catalog.Identifier;

import java.util.List;

public interface TableRepository<T> {
    List<TableRecord> findAll(T tx, String database);

    boolean delete(T tx, Identifier identifier);
}
