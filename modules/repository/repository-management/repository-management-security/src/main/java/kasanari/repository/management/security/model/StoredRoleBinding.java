package kasanari.repository.management.security.model;

import kasanari.catalog.management.model.CatalogType;
import kasanari.catalog.management.model.RoleBinding;

public record StoredRoleBinding(
        String subject,
        CatalogType catalogType,
        RoleBinding.RoleEnum role
) {
}
