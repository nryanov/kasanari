package kasanari.server.infrastructure.management;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.management.api.ManagementRestSecurityService;
import kasanari.catalog.management.dto.CatalogTypeDto;
import kasanari.catalog.management.dto.DeleteRolesRequestDto;
import kasanari.catalog.management.dto.GetRolesResponseDto;
import kasanari.catalog.management.dto.UpdateRolesRequestDto;
import kasanari.management.security.ManagementSecurityService;
import kasanari.repository.management.security.model.StoredRoleBinding;
import kasanari.server.infrastructure.http.ApiFallbacks;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ManagementSecurityServiceHandler implements ManagementRestSecurityService {
    private final ManagementSecurityService securityService;

    public ManagementSecurityServiceHandler(ManagementSecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public Response deleteRoles(DeleteRolesRequestDto deleteRolesRequest, SecurityContext securityContext) {
        if (deleteRolesRequest == null || deleteRolesRequest.getBindings() == null || deleteRolesRequest.getBindings().isEmpty()) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, "Role bindings are required");
        }

        var subject = securityService.subject(securityContext);
        final List<StoredRoleBinding> bindings;
        try {
            bindings = RoleBindingMapper.toDomain(deleteRolesRequest.getBindings());
        } catch (IllegalArgumentException e) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, e.getMessage());
        }
        for (var type : distinctTypes(bindings)) {
            if (!securityService.canSecurityWrite(subject, type, "delete")) {
                return ApiFallbacks.error(Response.Status.FORBIDDEN, "Missing permission to delete role bindings");
            }
        }

        securityService.deleteRoles(bindings);
        securityService.reloadGroupingPolicies();

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response getRoles(String subject, CatalogTypeDto catalogType, SecurityContext securityContext) {
        var caller = securityService.subject(securityContext);
        var visibleDomains = getReadableDomains(caller);

        if (visibleDomains.isEmpty()) {
            return ApiFallbacks.error(Response.Status.FORBIDDEN, "Missing permission to read role bindings");
        }

        if (catalogType != null && !visibleDomains.contains(RoleBindingMapper.toDomain(catalogType))) {
            return ApiFallbacks.error(Response.Status.FORBIDDEN, "Missing permission to read role bindings");
        }

        var bindings = securityService.listRoles(subject, catalogType == null ? null : RoleBindingMapper.toDomain(catalogType));

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

        var subject = securityService.subject(securityContext);
        final List<StoredRoleBinding> bindings;
        try {
            bindings = RoleBindingMapper.toDomain(updateRolesRequest.getBindings());
        } catch (IllegalArgumentException e) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, e.getMessage());
        }
        for (var type : distinctTypes(bindings)) {
            if (!securityService.canSecurityWrite(subject, type, "update")) {
                return ApiFallbacks.error(Response.Status.FORBIDDEN, "Missing permission to update role bindings");
            }
        }

        securityService.updateRoles(bindings);
        securityService.reloadGroupingPolicies();

        var response = new GetRolesResponseDto();
        response.setBindings(bindings.stream().map(RoleBindingMapper::toApi).toList());
        return Response.status(Response.Status.OK).entity(response).build();
    }

    private Set<kasanari.repository.management.common.model.CatalogType> getReadableDomains(String subject) {
        var result = new java.util.HashSet<kasanari.repository.management.common.model.CatalogType>();
        for (var type : kasanari.repository.management.common.model.CatalogType.values()) {
            if (securityService.canSecurityRead(subject, type)) {
                result.add(type);
            }
        }
        return result;
    }

    private Set<kasanari.repository.management.common.model.CatalogType> distinctTypes(List<StoredRoleBinding> bindings) {
        return bindings.stream().map(StoredRoleBinding::catalogType).collect(Collectors.toSet());
    }
}
