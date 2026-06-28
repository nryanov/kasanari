package kasanari.server.infrastructure.management;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.authorization.runtime.AuthorizationService;
import kasanari.authorization.spi.Permission;
import kasanari.authorization.spi.RoleBinding;
import kasanari.catalog.management.api.ManagementRestSecurityService;
import kasanari.catalog.management.dto.AddRolesRequestDto;
import kasanari.catalog.management.dto.DeleteRolesRequestDto;
import kasanari.catalog.management.dto.GetRolesResponseDto;
import kasanari.server.infrastructure.http.ApiFallbacks;

import java.util.List;

@ApplicationScoped
public class ManagementSecurityServiceHandler implements ManagementRestSecurityService {
    private final AuthorizationService authorizationService;

    public ManagementSecurityServiceHandler(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public Response getRoles(String subject, String resource, SecurityContext securityContext) {
        if (resource == null || resource.isBlank()) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, "Resource is required");
        }

        var denied = authorizationService.denyUnless(securityContext, resource, Permission.RoleBindingGet);
        if (denied.isPresent()) {
            return denied.get();
        }

        var roleBindings = authorizationService.roleBindingsOrThrow();
        var bindings = roleBindings.list(subject, resource);

        var response = new GetRolesResponseDto();
        response.setBindings(bindings.stream().map(RoleBindingMapper::toApi).toList());

        return Response.status(Response.Status.OK).entity(response).build();
    }

    @Override
    public Response addRoles(AddRolesRequestDto addRolesRequest, SecurityContext securityContext) {
        if (addRolesRequest == null || addRolesRequest.getBindings() == null || addRolesRequest.getBindings().isEmpty()) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, "Role bindings are required");
        }

        final List<RoleBinding> bindings;
        try {
            bindings = RoleBindingMapper.toSpi(addRolesRequest.getBindings());
        } catch (IllegalArgumentException e) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, e.getMessage());
        }

        for (var binding : bindings) {
            var denied = authorizationService.denyUnless(securityContext, binding.resource(), Permission.RoleBindingAdd);
            if (denied.isPresent()) {
                return denied.get();
            }
        }

        var roleBindings = authorizationService.roleBindingsOrThrow();
        roleBindings.add(bindings);
        roleBindings.reloadPolicies();

        return Response.status(Response.Status.NO_CONTENT).build();
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

        for (var binding : bindings) {
            var denied = authorizationService.denyUnless(securityContext, binding.resource(), Permission.RoleBindingDelete);
            if (denied.isPresent()) {
                return denied.get();
            }
        }

        var roleBindings = authorizationService.roleBindingsOrThrow();
        roleBindings.delete(bindings);
        roleBindings.reloadPolicies();

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
