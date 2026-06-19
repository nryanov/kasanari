package kasanari.authorization.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.spi.AuthorizationRequest;
import kasanari.authorization.spi.AuthorizationResource;
import kasanari.authorization.spi.Permission;
import kasanari.authorization.spi.RoleBindingAdministration;
import kasanari.core.model.CatalogType;

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

    public boolean isAuthorized(String subject, String resource, Permission permission) {
        return registry.activeProvider().isAuthorized(new AuthorizationRequest(subject, resource, permission));
    }

    public boolean isAuthorized(SecurityContext securityContext, String resource, Permission permission) {
        return isAuthorized(subject(securityContext), resource, permission);
    }

    public boolean isAuthorized(SecurityContext securityContext, CatalogType domain, Permission permission) {
        return isAuthorized(
                subject(securityContext),
                AuthorizationResource.catalog(domain, "*").path(),
                permission
        );
    }

    public Optional<Response> denyUnless(SecurityContext securityContext, String resource, Permission permission) {
        if (isAuthorized(securityContext, resource, permission)) {
            return Optional.empty();
        }
        return Optional.of(Response.status(Response.Status.FORBIDDEN)
                .entity(java.util.Map.of(
                        "message",
                        "Missing permission: " + permission.wireName() + " on " + resource))
                .build());
    }

    public Optional<Response> denyUnless(SecurityContext securityContext, CatalogType domain, Permission permission) {
        return denyUnless(securityContext, AuthorizationResource.catalog(domain, "*").path(), permission);
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
