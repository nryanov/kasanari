package kasanari.repository.lance;

import org.lance.namespace.model.TableVersion;
import org.lance.namespace.model.VersionRange;

import java.util.List;

public interface TableVersionRepository<T> {
    void create(T tx, String tableId, TableVersion version);

    TableVersion get(T tx, String tableId, Long version);

    List<TableVersion> list(T tx, String tableId, boolean descending, Integer limit, String pageToken);

    long deleteRanges(T tx, String tableId, List<VersionRange> ranges);

    void deleteForTable(T tx, String tableId);
}
