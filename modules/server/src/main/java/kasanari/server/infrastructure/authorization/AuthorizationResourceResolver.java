package kasanari.server.infrastructure.authorization;

import kasanari.authorization.spi.AuthorizationResource;
import kasanari.core.model.CatalogType;

import java.util.Map;

public final class AuthorizationResourceResolver {
    private AuthorizationResourceResolver() {
    }

    public static String resolve(CatalogType catalogType, String catalogName, Map<String, String> attributes) {
        var attrs = attributes == null ? Map.<String, String>of() : attributes;

        if (attrs.containsKey("to")) {
            return parseIdentifier(catalogType, catalogName, attrs.get("to"));
        }
        if (attrs.containsKey("from")) {
            return parseIdentifier(catalogType, catalogName, attrs.get("from"));
        }

        var namespace = firstNonBlank(attrs.get("namespace"), attrs.get("database"));
        if (namespace == null && attrs.containsKey("parent")) {
            var parent = attrs.get("parent");
            if (parent != null && !parent.isBlank() && !"null".equals(parent)) {
                namespace = parent;
            }
        }

        var objectName = firstNonBlank(attrs.get("table"), attrs.get("view"), attrs.get("function"));
        if (objectName != null && namespace != null) {
            return AuthorizationResource.object(catalogType, catalogName, namespace, objectName).path();
        }
        if (namespace != null) {
            return AuthorizationResource.namespace(catalogType, catalogName, namespace).path();
        }
        return AuthorizationResource.catalog(catalogType, catalogName).path();
    }

    private static String parseIdentifier(CatalogType catalogType, String catalogName, String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return AuthorizationResource.catalog(catalogType, catalogName).path();
        }
        var lastDot = identifier.lastIndexOf('.');
        if (lastDot <= 0 || lastDot == identifier.length() - 1) {
            return AuthorizationResource.catalog(catalogType, catalogName).path();
        }
        var namespace = identifier.substring(0, lastDot);
        var objectName = identifier.substring(lastDot + 1);
        return AuthorizationResource.object(catalogType, catalogName, namespace, objectName).path();
    }

    private static String firstNonBlank(String... values) {
        for (var value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
