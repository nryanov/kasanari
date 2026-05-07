package kasanari.catalog.paimon.repository;

import kasanari.catalog.paimon.model.TagRecord;
import org.apache.paimon.catalog.Identifier;

import java.util.List;
import java.util.Optional;

public interface TagRepository<T> {
    void create(T tx, TagRecord record, boolean ignoreIfExists);

    boolean delete(T tx, Identifier identifier, String tagName);

    Optional<TagRecord> find(T tx, Identifier identifier, String tagName);

    boolean exists(T tx, Identifier identifier, String tagName);

    List<String> findAll(T tx, Identifier identifier, Optional<String> tagNamePrefix);
}
