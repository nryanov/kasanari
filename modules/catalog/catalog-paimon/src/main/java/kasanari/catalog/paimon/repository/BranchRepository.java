package kasanari.catalog.paimon.repository;

import org.apache.paimon.catalog.Identifier;

import javax.annotation.Nullable;
import java.util.List;

public interface BranchRepository<T> {
    void create(T tx, Identifier identifier, String branch, @Nullable String fromTag);

    boolean delete(T tx, Identifier identifier, String branch);

    boolean rename(T tx, Identifier identifier, String fromBranch, String toBranch);

    boolean fastForward(T tx, Identifier identifier, String branch);

    boolean exists(T tx, Identifier identifier, String branch);

    List<String> findAll(T tx, Identifier identifier);
}
