package kasanari.repository.lance.model;

import java.util.Map;

public record TransactionRow(String id, String status, Map<String, String> properties) {
}
