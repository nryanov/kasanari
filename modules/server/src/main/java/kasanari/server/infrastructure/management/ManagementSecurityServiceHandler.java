package kasanari.server.infrastructure.management;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.authorization.spi.RoleBinding;
import kasanari.catalog.management.api.ManagementRestSecurityService;
import kasanari.catalog.management.dto.CatalogTypeDto;
import kasanari.catalog.management.dto.DeleteRolesRequestDto;
import kasanari.catalog.management.dto.GetRolesResponseDto;
import kasanari.catalog.management.dto.UpdateRolesRequestDto;
import kasanari.repository.management.common.model.CatalogType;
import kasanari.server.infrastructure.http.ApiFallbacks;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Response getRoles(String subject, CatalogTypeDto catalogType, SecurityContext securityContext) {
        var visibleDomains = getReadableDomains(securityContext);

        if (visibleDomains.isEmpty()) {
            return ApiFallbacks.error(Response.Status.FORBIDDEN, "Missing permission to read role bindings");
        }

        if (catalogType != null && !visibleDomains.contains(RoleBindingMapper.toDomain(catalogType))) {
            return ApiFallbacks.error(Response.Status.FORBIDDEN, "Missing permission to read role bindings");
        }

        var roleBindings = authorizationService.roleBindingsOrThrow();
        var bindings = roleBindings.list(subject, catalogType == null ? null : RoleBindingMapper.toDomain(catalogType));

        var filtered = bindings.stream()
                .filter(binding -> visibleDomains.contains(binding.catalogType()))
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
        var result = new java.util.HashSet<CatalogType>();
        for (var type : CatalogType.values()) {
            if (authorizationService.isAuthorized(securityContext, type, Permission.RoleSelect)) {
                result.add(type);
            }
        }
        return result;
    }

    private Set<CatalogType> distinctTypes(List<RoleBinding> bindings) {
        return bindings.stream().map(RoleBinding::catalogType).collect(Collectors.toSet());
    }
}
