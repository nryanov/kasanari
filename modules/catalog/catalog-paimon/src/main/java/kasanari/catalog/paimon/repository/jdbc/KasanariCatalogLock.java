package kasanari.catalog.paimon.repository.jdbc;

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
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
