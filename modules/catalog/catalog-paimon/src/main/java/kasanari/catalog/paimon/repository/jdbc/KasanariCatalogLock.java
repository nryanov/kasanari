package kasanari.catalog.paimon.repository.jdbc;

import org.apache.paimon.catalog.CatalogLock;
import org.jdbi.v3.core.Handle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.zip.CRC32;

public class KasanariCatalogLock implements CatalogLock {
    private final Handle handle;

    public KasanariCatalogLock(Handle handle) {
        this.handle = handle;
    }

    @Override
    public <T> T runWithLock(String database, String table, Callable<T> callable) throws Exception {
        var lockId = advisoryLockKey(database, table);
        handle.createQuery(JdbcQueries.ACQUIRE_TRANSACTIONAL_ADVISORY_LOCK)
                .bind(0, lockId)
                .mapTo(Integer.class)
                .first();
        return callable.call();
    }

    @Override
    public void close() throws IOException {
        // PostgreSQL transactional advisory locks are released automatically
        // at transaction end, so no explicit unlock is needed here.
    }

    private long advisoryLockKey(String database, String table) {
        var crc32 = new CRC32();
        var key = database + "." + table;
        crc32.update(key.getBytes(StandardCharsets.UTF_8));
        return crc32.getValue();
    }
}
