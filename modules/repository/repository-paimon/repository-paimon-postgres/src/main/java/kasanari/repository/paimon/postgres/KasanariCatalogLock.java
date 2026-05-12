package kasanari.repository.paimon.postgres;

import org.apache.paimon.catalog.CatalogLock;
import org.jdbi.v3.core.Handle;

import java.io.IOException;
import java.util.concurrent.Callable;

public class KasanariCatalogLock implements CatalogLock {
    private final Handle handle;

    public KasanariCatalogLock(Handle handle) {
        this.handle = handle;
    }

    @Override
    public <T> T runWithLock(String database, String table, Callable<T> callable) throws Exception {
        var lockId = database + "." + table;
        handle.createCall(JdbcQueries.ACQUIRE_TRANSACTIONAL_ADVISORY_LOCK)
                .bind(0, lockId)
                .invoke();
        return callable.call();
    }

    @Override
    public void close() throws IOException {
        // PostgreSQL transactional advisory locks are released automatically
        // at transaction end, so no explicit unlock is needed here.
    }
}
