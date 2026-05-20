package kasanari.authorization.spi;

import kasanari.repository.management.common.model.CatalogType;

public record AuthorizationRequest(
        String subject,
        CatalogType domain,
        Permission permission
) {
    public AuthorizationRequest(String subject, CatalogType domain, String permissionName) {
        this(subject, domain, Permission.fromName(permissionName));
    }
}
