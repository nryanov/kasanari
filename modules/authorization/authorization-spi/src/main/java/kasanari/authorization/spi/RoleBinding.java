package kasanari.authorization.spi;

import kasanari.repository.management.common.model.CatalogType;

public record RoleBinding(
        String subject,
        CatalogType catalogType,
        String role
) {
}
