package kasanari.server.infrastructure.lance;

import java.util.regex.Pattern;

public abstract class LanceCatalogHelper {
    public record ParsedCatalogNamespaceTableId(String catalog, String namespace, String table) { }

    public static ParsedCatalogNamespaceTableId parseCatalogNamespaceTableId(String id, String delimiter) {
        var actualDelimiter = (delimiter == null || delimiter.isBlank()) ? "." : delimiter;
        var parts = id.split(Pattern.quote(actualDelimiter), -1);
        if (parts.length != 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
            throw new IllegalArgumentException("Invalid id format. Expected 'catalog" + actualDelimiter + "namespace" + actualDelimiter + "table'");
        }
        return new ParsedCatalogNamespaceTableId(parts[0], parts[1], parts[2]);
    }
}
