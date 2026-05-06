package kasanari.catalog.paimon.model;

import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.function.Function;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record FunctionRecord(
        String database,
        String name,
        boolean deterministic,
        Map<String, FunctionDefinition> definitions,
        Optional<String> comment,
        Map<String, String> options
) {

    public FunctionRecord(Identifier identifier, Function function) {
    }

    public sealed interface FunctionDefinition permits
            FunctionDefinition.File,
            FunctionDefinition.Sql,
            FunctionDefinition.Lambda {
        record File(List<Resource> fileResources, String language, String className, String functionName) implements FunctionDefinition {
            record Resource(String resourceType, String uri) {}
        }

        record Sql(String definition) implements FunctionDefinition {
        }

        record Lambda(String definition, String language) implements FunctionDefinition {
        }
    }
}
