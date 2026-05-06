package kasanari.catalog.paimon.model;

import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.function.Function;

import java.util.HashMap;
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
        this(
                identifier.getDatabaseName(),
                identifier.getObjectName(),
                function.isDeterministic(),
                resolveDefinitions(function),
                Optional.ofNullable(function.comment()),
                function.options()
        );
    }

    private static Map<String, FunctionDefinition> resolveDefinitions(Function function) {
        var result = new HashMap<String, FunctionDefinition>();
        function.definitions().forEach((name, def) -> result.put(name, fromPaimon(def)));

        return result;
    }

    public static FunctionDefinition fromPaimon(org.apache.paimon.function.FunctionDefinition def) {
        return switch (def) {
            case org.apache.paimon.function.FunctionDefinition.FileFunctionDefinition i -> {
                var resources = i.fileResources().stream().map(it -> new FunctionDefinition.File.Resource(it.resourceType(), it.uri())).toList();
                yield new FunctionDefinition.File(
                        resources,
                        i.language(),
                        i.className(),
                        i.functionName()
                );
            }
            case org.apache.paimon.function.FunctionDefinition.SQLFunctionDefinition i -> new FunctionDefinition.Sql(i.definition());
            case org.apache.paimon.function.FunctionDefinition.LambdaFunctionDefinition i -> new FunctionDefinition.Lambda(i.definition(), i.language());
            default -> throw new IllegalArgumentException("Unknown function definition");
        };
    }

    public sealed interface FunctionDefinition permits
            FunctionDefinition.File,
            FunctionDefinition.Sql,
            FunctionDefinition.Lambda {
        org.apache.paimon.function.FunctionDefinition toPaimon();

        record File(List<Resource> fileResources, String language, String className,
                    String functionName) implements FunctionDefinition {
            record Resource(String resourceType, String uri) {
            }

            @Override
            public org.apache.paimon.function.FunctionDefinition toPaimon() {
                return new org.apache.paimon.function.FunctionDefinition.FileFunctionDefinition(
                        fileResources
                                .stream()
                                .map(it -> new org.apache.paimon.function.FunctionDefinition.FunctionFileResource(it.resourceType, it.uri))
                                .toList(),
                        language,
                        className,
                        functionName
                );
            }
        }

        record Sql(String definition) implements FunctionDefinition {
            @Override
            public org.apache.paimon.function.FunctionDefinition toPaimon() {
                return new org.apache.paimon.function.FunctionDefinition.SQLFunctionDefinition(definition);
            }
        }

        record Lambda(String definition, String language) implements FunctionDefinition {
            @Override
            public org.apache.paimon.function.FunctionDefinition toPaimon() {
                return new org.apache.paimon.function.FunctionDefinition.LambdaFunctionDefinition(definition, language);
            }
        }
    }
}
