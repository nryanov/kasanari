package kasanari.authorization.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.spi.AuthorizationRequest;
import kasanari.authorization.spi.Permission;
import kasanari.authorization.spi.RoleBindingAdministration;
import kasanari.repository.management.common.model.CatalogType;

import java.util.Optional;

@ApplicationScoped
public class AuthorizationService {
    private final AuthorizationProviderRegistry registry;

    @Inject
    public AuthorizationService(AuthorizationProviderRegistry registry) {
        this.registry = registry;
    }

    public String subject(SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return "anonymous";
        }
        return securityContext.getUserPrincipal().getName();
    }

    public boolean isAuthorized(String subject, CatalogType domain, Permission permission) {
        return registry.activeProvider().isAuthorized(new AuthorizationRequest(subject, domain, permission));
    }

    public boolean isAuthorized(SecurityContext securityContext, CatalogType domain, Permission permission) {
        return isAuthorized(subject(securityContext), domain, permission);
    }

    public Optional<Response> denyUnless(SecurityContext securityContext, CatalogType domain, Permission permission) {
        if (isAuthorized(securityContext, domain, permission)) {
            return Optional.empty();
        }
        return Optional.of(Response.status(Response.Status.FORBIDDEN)
                .entity(java.util.Map.of("message", "Missing permission: " + permission.wireName()))
                .build());
    }

    public RoleBindingAdministration roleBindingsOrThrow() {
        return registry.activeProvider()
                .roleBindings()
                .orElseThrow(() -> new IllegalStateException(
                        "Active authorization provider '" + registry.activeProvider().type()
                                + "' does not support role binding administration"));
    }

    public Optional<RoleBindingAdministration> roleBindings() {
        return registry.activeProvider().roleBindings();
    }
}
