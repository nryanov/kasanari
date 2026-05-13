package kasanari.repository.lance.postgres;

import kasanari.repository.lance.TransactionRepository;
import kasanari.repository.lance.model.TransactionRow;
import org.jdbi.v3.core.Handle;

import java.util.Map;

public class JdbcTransactionRepository implements TransactionRepository<Handle> {
    public static final String STATUS_QUEUED = "Queued";
    public static final String STATUS_SUCCEEDED = "Succeeded";

    public JdbcTransactionRepository() {
    }

    @Override
    public void upsert(Handle tx, String transactionId, String status, Map<String, String> properties) {
        tx.createUpdate("""
                        INSERT INTO kasanari_lance_transactions(transaction_id, status, properties)
                        VALUES (:transaction_id, :status, :properties)
                        ON CONFLICT (transaction_id)
                        DO UPDATE SET status = EXCLUDED.status, properties = EXCLUDED.properties
                        """)
                .bind("transaction_id", transactionId)
                .bind("status", status)
                .bind("properties", PropertiesSerde.encode(properties))
                .execute();
    }

    @Override
    public TransactionRow get(Handle tx, String transactionId) {
        return tx.createQuery("""
                        SELECT transaction_id, status, properties
                        FROM kasanari_lance_transactions
                        WHERE transaction_id = :transaction_id
                        LIMIT 1
                        """)
                .bind("transaction_id", transactionId)
                .map((rs, ctx) -> new TransactionRow(
                        rs.getString("transaction_id"),
                        rs.getString("status"),
                        PropertiesSerde.decode(rs.getString("properties"))
                ))
                .findOne()
                .orElse(null);
    }
}
