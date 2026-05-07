package kasanari.catalog.paimon.model;

import java.util.Map;
import java.util.Optional;

public record DatabaseRecord(String name, Map<String, String> options, Optional<String> comment) {
}
