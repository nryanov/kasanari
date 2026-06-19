package kasanari.authorization.spi;

import kasanari.core.model.CatalogType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class AuthorizationResource {
    private static final String SCOPE_SUFFIX = "/*";

    private final CatalogType catalogType;
    private final List<String> segments;

    private AuthorizationResource(CatalogType catalogType, List<String> segments) {
        this.catalogType = catalogType;
        this.segments = List.copyOf(segments);
    }

    public static AuthorizationResource catalog(CatalogType catalogType, String catalogName) {
        validateSegment(catalogName, "catalogName");
        return new AuthorizationResource(catalogType, List.of(catalogType.toString(), catalogName));
    }

    public static AuthorizationResource namespace(CatalogType catalogType, String catalogName, String namespace) {
        validateSegment(catalogName, "catalogName");
        validateSegment(namespace, "namespace");
        return new AuthorizationResource(catalogType, List.of(catalogType.toString(), catalogName, namespace));
    }

    public static AuthorizationResource object(
            CatalogType catalogType,
            String catalogName,
            String namespace,
            String objectName
    ) {
        validateSegment(catalogName, "catalogName");
        validateSegment(namespace, "namespace");
        validateSegment(objectName, "objectName");
        return new AuthorizationResource(
                catalogType,
                List.of(catalogType.toString(), catalogName, namespace, objectName)
        );
    }

    public static AuthorizationResource parse(String resource) {
        Objects.requireNonNull(resource, "resource");
        var trimmed = resource.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Resource must not be blank");
        }

        var scopePattern = trimmed.endsWith(SCOPE_SUFFIX);
        var path = scopePattern ? trimmed.substring(0, trimmed.length() - SCOPE_SUFFIX.length()) : trimmed;
        if (path.isEmpty()) {
            throw new IllegalArgumentException("Invalid resource scope pattern: " + resource);
        }

        var parts = path.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Resource must include catalog type and catalog name: " + resource);
        }

        var catalogType = CatalogType.fromValue(parts[0]);
        var segments = new ArrayList<String>();
        for (var part : parts) {
            validateSegment(part, "resource segment");
            segments.add(part);
        }

        return new AuthorizationResource(catalogType, segments);
    }

    public CatalogType catalogType() {
        return catalogType;
    }

    public String path() {
        return String.join("/", segments);
    }

    public String scopePattern() {
        return path() + SCOPE_SUFFIX;
    }

    public boolean isScopePattern(String value) {
        return value != null && value.endsWith(SCOPE_SUFFIX) && parse(value).path().equals(path());
    }

    private static void validateSegment(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        if (value.contains("/")) {
            throw new IllegalArgumentException(name + " must not contain '/'");
        }
    }
}
