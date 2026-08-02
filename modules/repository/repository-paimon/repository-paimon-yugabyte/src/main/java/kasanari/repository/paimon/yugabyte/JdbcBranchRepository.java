package kasanari.repository.paimon.yugabyte;

import kasanari.repository.paimon.model.BranchRecord;
import kasanari.repository.paimon.BranchRepository;
import org.apache.paimon.catalog.Identifier;
import org.jdbi.v3.core.Handle;

import java.util.List;
import java.util.Optional;

public class JdbcBranchRepository implements BranchRepository<Handle> {
    private final String catalogKey;

    public JdbcBranchRepository(String catalogKey) {
        this.catalogKey = catalogKey;
    }

    @Override
    public void create(Handle tx, BranchRecord record) {
        var query = tx.createUpdate(JdbcQueries.INSERT_BRANCH);
        query.bind(0, catalogKey);
        query.bind(1, record.database());
        query.bind(2, record.table());
        query.bind(3, record.branchName());
        query.bind(4, record.tagName().orElse(null));
        query.execute();
    }

    @Override
    public boolean delete(Handle tx, Identifier identifier, String branch) {
        var query = tx.createUpdate(JdbcQueries.DELETE_BRANCH);
        query.bind(0, catalogKey);
        query.bind(1, identifier.getDatabaseName());
        query.bind(2, identifier.getTableName());
        query.bind(3, branch);
        return query.execute() == 1;
    }

    @Override
    public boolean rename(Handle tx, Identifier identifier, String fromBranch, String toBranch) {
        var query = tx.createUpdate(JdbcQueries.RENAME_BRANCH);
        query.bind(0, toBranch);
        query.bind(1, catalogKey);
        query.bind(2, identifier.getDatabaseName());
        query.bind(3, identifier.getTableName());
        query.bind(4, fromBranch);
        return query.execute() == 1;
    }

    @Override
    public boolean fastForward(Handle tx, Identifier identifier, String branch) {
        var query = tx.createUpdate(JdbcQueries.FAST_FORWARD_BRANCH);
        query.bind(0, catalogKey);
        query.bind(1, identifier.getDatabaseName());
        query.bind(2, identifier.getTableName());
        query.bind(3, branch);
        return query.execute() == 1;
    }

    @Override
    public List<BranchRecord> findAll(Handle tx, Identifier identifier) {
        var query = tx.createQuery(JdbcQueries.LIST_BRANCHES);
        query.bind(0, catalogKey);
        query.bind(1, identifier.getDatabaseName());
        query.bind(2, identifier.getTableName());
        return query
                .map((rs, ctx) -> new BranchRecord(
                        identifier.getDatabaseName(),
                        identifier.getTableName(),
                        rs.getString("branch_name"),
                        Optional.ofNullable(rs.getString("tag_name"))
                ))
                .list();
    }
}
