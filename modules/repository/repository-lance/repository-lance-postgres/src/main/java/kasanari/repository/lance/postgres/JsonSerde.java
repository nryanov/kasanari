package kasanari.repository.lance.postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public final class JsonSerde {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonSerde() {
    }

    public static String encodeMap(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return "{}";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize JSON payload", e);
        }
    }

    public static Map<String, String> decodeMap(String payload) {
        if (payload == null || payload.isBlank()) {
            return new HashMap<>();
        }
        try {
            return OBJECT_MAPPER.readValue(payload, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize JSON payload", e);
        }
    }
}
