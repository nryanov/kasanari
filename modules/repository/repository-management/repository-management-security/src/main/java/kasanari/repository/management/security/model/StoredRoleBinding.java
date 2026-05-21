package kasanari.repository.management.security.model;

import kasanari.core.model.CatalogType;

public record StoredRoleBinding(
        String subject,
        CatalogType catalogType,
        String role
) {
}
