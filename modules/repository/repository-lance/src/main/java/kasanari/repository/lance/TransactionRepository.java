package kasanari.repository.lance;

import kasanari.repository.lance.model.TransactionRow;

import java.util.Map;

public interface TransactionRepository<T> {
    void upsert(T tx, String transactionId, String status, Map<String, String> properties);

    TransactionRow get(T tx, String transactionId);
}
