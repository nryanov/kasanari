package kasanari.repository.management.security.model;

import kasanari.repository.management.common.model.CatalogType;

public record StoredRoleBinding(
        String subject,
        CatalogType catalogType,
        Role role
) {
}
