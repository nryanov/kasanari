package kasanari.authorization.spi;

import kasanari.core.model.CatalogType;

public record RoleBinding(
        String subject,
        CatalogType catalogType,
        String role
) {
}
