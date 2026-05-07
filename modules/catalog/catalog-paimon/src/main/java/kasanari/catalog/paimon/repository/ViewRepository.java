package kasanari.catalog.paimon.repository;

import kasanari.catalog.paimon.model.ViewRecord;
import org.apache.paimon.catalog.Identifier;

import java.util.List;
import java.util.Optional;

public interface ViewRepository<T> {
    List<ViewRecord> findAll(T tx, String database);

    boolean delete(T tx, Identifier identifier);

    void create(T tx, ViewRecord record);

    void alter(T tx, ViewRecord record);

    boolean rename(T tx, Identifier from, Identifier to);

    Optional<ViewRecord> find(T tx, Identifier view);
}
