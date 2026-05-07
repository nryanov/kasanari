package kasanari.catalog.paimon.repository;

import kasanari.catalog.paimon.model.BranchRecord;
import org.apache.paimon.catalog.Identifier;

import javax.annotation.Nullable;
import java.util.List;

public interface BranchRepository<T> {
    void create(T tx, BranchRecord record);

    boolean delete(T tx, Identifier identifier, String branch);

    boolean rename(T tx, Identifier identifier, String fromBranch, String toBranch);

    boolean fastForward(T tx, Identifier identifier, String branch);

    List<BranchRecord> findAll(T tx, Identifier identifier);
}
