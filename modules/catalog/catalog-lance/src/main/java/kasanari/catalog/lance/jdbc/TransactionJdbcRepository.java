package kasanari.catalog.lance.jdbc;

import kasanari.repository.jdbc.KasanariDataSource;

import java.util.HashMap;
import java.util.Map;

public class TransactionJdbcRepository {
    public static final String STATUS_QUEUED = "Queued";
    public static final String STATUS_SUCCEEDED = "Succeeded";

    private final KasanariDataSource dataSource;

    public TransactionJdbcRepository(KasanariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void upsert(String transactionId, String status, Map<String, String> properties) {
        dataSource.getJdbi().useHandle(handle -> handle.createUpdate("""
                        INSERT INTO kasanari_lance_transactions(transaction_id, status, properties)
                        VALUES (:transaction_id, :status, :properties)
                        ON CONFLICT (transaction_id)
                        DO UPDATE SET status = EXCLUDED.status, properties = EXCLUDED.properties
                        """)
                .bind("transaction_id", transactionId)
                .bind("status", status)
                .bind("properties", PropertiesSerde.encode(properties))
                .execute());
    }

    public TransactionRow get(String transactionId) {
        return dataSource.getJdbi().withHandle(handle -> handle.createQuery("""
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
                .orElse(null));
    }

    public record TransactionRow(String id, String status, Map<String, String> properties) {
        public Map<String, String> propertiesOrEmpty() {
            return properties == null ? new HashMap<>() : properties;
        }
    }
}
