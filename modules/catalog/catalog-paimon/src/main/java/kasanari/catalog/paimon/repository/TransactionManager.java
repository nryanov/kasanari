package kasanari.catalog.paimon.repository;

import java.util.function.Consumer;
import java.util.function.Function;

public interface TransactionManager<T> {
    <R> R inTransactionR(Function<T, R> block);

    void inTransaction(Consumer<T> block);
}
