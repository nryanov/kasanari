package kasanari.catalog.paimon.repository.jdbc;

import kasanari.catalog.paimon.repository.TransactionManager;
import org.jdbi.v3.core.Handle;

import java.util.function.Consumer;
import java.util.function.Function;

public class JdbcTransactionManager implements TransactionManager<Handle> {
    private final KasanariDataSource dataSource;

    public JdbcTransactionManager(KasanariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public <R> R inTransactionR(Function<Handle, R> block) {
        return dataSource.getJdbi().inTransaction(block::apply);
    }

    @Override
    public void inTransaction(Consumer<Handle> block) {
        dataSource.getJdbi().useTransaction(block::accept);
    }
}
