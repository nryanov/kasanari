package kasanari.server.infrastructure.management;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.AuthorizationResource;
import kasanari.authorization.spi.Permission;
import kasanari.authorization.spi.RoleBinding;
import kasanari.catalog.management.api.ManagementRestSecurityService;
import kasanari.catalog.management.dto.DeleteRolesRequestDto;
import kasanari.catalog.management.dto.GetRolesResponseDto;
import kasanari.catalog.management.dto.UpdateRolesRequestDto;
import kasanari.core.model.CatalogType;
import kasanari.server.infrastructure.http.ApiFallbacks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class ManagementSecurityServiceHandler implements ManagementRestSecurityService {
    private final AuthorizationService authorizationService;

    public ManagementSecurityServiceHandler(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public Response deleteRoles(DeleteRolesRequestDto deleteRolesRequest, SecurityContext securityContext) {
        if (deleteRolesRequest == null || deleteRolesRequest.getBindings() == null || deleteRolesRequest.getBindings().isEmpty()) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, "Role bindings are required");
        }

        final List<RoleBinding> bindings;
        try {
            bindings = RoleBindingMapper.toSpi(deleteRolesRequest.getBindings());
        } catch (IllegalArgumentException e) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, e.getMessage());
        }
        for (var type : distinctTypes(bindings)) {
            var denied = authorizationService.denyUnless(securityContext, type, Permission.RoleRemove);
            if (denied.isPresent()) {
                return denied.get();
            }
        }

        var roleBindings = authorizationService.roleBindingsOrThrow();
        roleBindings.delete(bindings);
        roleBindings.reloadPolicies();

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response getRoles(String subject, String resourcePrefix, SecurityContext securityContext) {
        var visibleDomains = getReadableDomains(securityContext);

        if (visibleDomains.isEmpty()) {
            return ApiFallbacks.error(Response.Status.FORBIDDEN, "Missing permission to read role bindings");
        }

        if (resourcePrefix != null && !resourcePrefix.isBlank()) {
            var prefixDomain = parseDomainPrefix(resourcePrefix);
            if (prefixDomain.isPresent() && !visibleDomains.contains(prefixDomain.get())) {
                return ApiFallbacks.error(Response.Status.FORBIDDEN, "Missing permission to read role bindings");
            }
        }

        var roleBindings = authorizationService.roleBindingsOrThrow();
        var bindings = roleBindings.list(subject, normalizeResourcePrefix(resourcePrefix));

        var filtered = bindings.stream()
                .filter(binding -> visibleDomains.contains(AuthorizationResource.parse(binding.resource()).catalogType()))
                .map(RoleBindingMapper::toApi)
                .toList();

        var response = new GetRolesResponseDto();
        response.setBindings(filtered);

        return Response.status(Response.Status.OK).entity(response).build();
    }

    @Override
    public Response updateRoles(UpdateRolesRequestDto updateRolesRequest, SecurityContext securityContext) {
        if (updateRolesRequest == null || updateRolesRequest.getBindings() == null || updateRolesRequest.getBindings().isEmpty()) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, "Role bindings are required");
        }

        final List<RoleBinding> bindings;
        try {
            bindings = RoleBindingMapper.toSpi(updateRolesRequest.getBindings());
        } catch (IllegalArgumentException e) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, e.getMessage());
        }
        for (var type : distinctTypes(bindings)) {
            var denied = authorizationService.denyUnless(securityContext, type, Permission.RoleAdd);
            if (denied.isPresent()) {
                return denied.get();
            }
        }

        var roleBindings = authorizationService.roleBindingsOrThrow();
        roleBindings.upsert(bindings);
        roleBindings.reloadPolicies();

        var response = new GetRolesResponseDto();
        response.setBindings(bindings.stream().map(RoleBindingMapper::toApi).toList());
        return Response.status(Response.Status.OK).entity(response).build();
    }

    private Set<CatalogType> getReadableDomains(SecurityContext securityContext) {
        var result = new HashSet<CatalogType>();
        for (var type : CatalogType.values()) {
            if (authorizationService.isAuthorized(securityContext, type, Permission.RoleSelect)) {
                result.add(type);
            }
        }
        return result;
    }
}
