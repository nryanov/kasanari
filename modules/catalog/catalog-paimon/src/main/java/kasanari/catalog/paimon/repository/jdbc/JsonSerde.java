package kasanari.catalog.paimon.repository.jdbc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kasanari.catalog.paimon.model.FunctionRecord;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonSerde {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonSerde() {
    }

    public static String encodeMap(Map<String, String> values) {
        return writeJson(values);
    }

    public static Map<String, String> decodeMap(String payload) {
        if (payload == null || payload.isBlank()) {
            return new LinkedHashMap<>();
        }
        return readJson(payload, new TypeReference<>() {
        });
    }

    public static String encodeDefinitions(Map<String, FunctionRecord.FunctionDefinition> definitions) {
        var stored = new LinkedHashMap<String, StoredFunctionDefinition>();
        definitions.forEach((name, definition) -> stored.put(name, toStoredDefinition(definition)));
        return writeJson(stored);
    }

    public static Map<String, FunctionRecord.FunctionDefinition> decodeDefinitions(String payload) {
        if (payload == null || payload.isBlank()) {
            return new LinkedHashMap<>();
        }

        var stored = readJson(payload, new TypeReference<Map<String, StoredFunctionDefinition>>() {
        });
        var result = new LinkedHashMap<String, FunctionRecord.FunctionDefinition>();
        stored.forEach((name, definition) -> result.put(name, fromStoredDefinition(definition)));
        return result;
    }

    private static StoredFunctionDefinition toStoredDefinition(FunctionRecord.FunctionDefinition definition) {
        return switch (definition) {
            case FunctionRecord.FunctionDefinition.Sql sql ->
                    new StoredFunctionDefinition("sql", sql.definition(), null, null, null, List.of());
            case FunctionRecord.FunctionDefinition.Lambda lambda ->
                    new StoredFunctionDefinition("lambda", lambda.definition(), lambda.language(), null, null, List.of());
            case FunctionRecord.FunctionDefinition.File file -> {
                var resources = file.fileResources().stream()
                        .map(resource -> new StoredFunctionResource(resource.resourceType(), resource.uri()))
                        .toList();
                yield new StoredFunctionDefinition(
                        "file",
                        null,
                        file.language(),
                        file.className(),
                        file.functionName(),
                        resources
                );
            }
        };
    }

    private static FunctionRecord.FunctionDefinition fromStoredDefinition(StoredFunctionDefinition definition) {
        return switch (definition.type) {
            case "sql" -> new FunctionRecord.FunctionDefinition.Sql(definition.definition);
            case "lambda" -> new FunctionRecord.FunctionDefinition.Lambda(definition.definition, definition.language);
            case "file" -> {
                var resources = definition.fileResources.stream()
                        .map(resource -> new FunctionRecord.FunctionDefinition.File.Resource(resource.resourceType, resource.uri))
                        .toList();
                yield new FunctionRecord.FunctionDefinition.File(
                        resources,
                        definition.language,
                        definition.className,
                        definition.functionName
                );
            }
            default -> throw new IllegalArgumentException("Unknown function definition type: " + definition.type);
        };
    }

    private static String writeJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize JSON payload", e);
        }
    }

    private static <T> T readJson(String payload, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(payload, typeReference);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize JSON payload", e);
        }
    }

    private record StoredFunctionDefinition(
            String type,
            String definition,
            String language,
            String className,
            String functionName,
            List<StoredFunctionResource> fileResources
    ) {
    }

    private record StoredFunctionResource(String resourceType, String uri) {
    }
}
