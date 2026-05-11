package kasanari.server.management;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.management.model.CatalogType;

@ApplicationScoped
public class ManagementAuthorizationService {
    private final ManagementInfrastructure infrastructure;

    public ManagementAuthorizationService(ManagementInfrastructure infrastructure) {
        this.infrastructure = infrastructure;
    }

    public String subject(SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return "anonymous";
        }
        return securityContext.getUserPrincipal().getName();
    }

    public boolean canCatalogRead(String subject, CatalogType catalogType) {
        return can(subject, catalogType, "catalog", "get");
    }

    public boolean canCatalogWrite(String subject, CatalogType catalogType, String action) {
        return can(subject, catalogType, "catalog", action);
    }

    public boolean canSecurityRead(String subject, CatalogType catalogType) {
        return can(subject, catalogType, "security.roles", "get");
    }

    public boolean canSecurityWrite(String subject, CatalogType catalogType, String action) {
        return can(subject, catalogType, "security.roles", action);
    }

    private boolean can(String subject, CatalogType catalogType, String obj, String action) {
        return infrastructure.enforcer().enforce(subject, catalogType.toString(), obj, action);
    }
}
