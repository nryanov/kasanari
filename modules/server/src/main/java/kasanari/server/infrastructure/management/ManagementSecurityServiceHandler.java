package kasanari.server.infrastructure.management;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import kasanari.catalog.management.api.ManagementRestSecurityService;
import kasanari.catalog.management.model.CatalogType;
import kasanari.catalog.management.model.DeleteRolesRequest;
import kasanari.catalog.management.model.GetRolesResponse;
import kasanari.catalog.management.model.RoleBinding;
import kasanari.catalog.management.model.UpdateRolesRequest;
import kasanari.management.security.ManagementSecurityService;
import kasanari.repository.management.security.model.StoredRoleBinding;
import kasanari.server.infrastructure.http.ApiFallbacks;

import java.util.ArrayList;
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
    public Response deleteRoles(DeleteRolesRequest deleteRolesRequest, SecurityContext securityContext) {
        if (deleteRolesRequest == null || deleteRolesRequest.getBindings() == null || deleteRolesRequest.getBindings().isEmpty()) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, "Role bindings are required");
        }

        var subject = securityService.subject(securityContext);
        final List<StoredRoleBinding> bindings;
        try {
            bindings = toStoredBindings(deleteRolesRequest.getBindings());
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
    public Response getRoles(String subject, CatalogType catalogType, SecurityContext securityContext) {
        var caller = securityService.subject(securityContext);
        var visibleDomains = getReadableDomains(caller);

        if (visibleDomains.isEmpty()) {
            return ApiFallbacks.error(Response.Status.FORBIDDEN, "Missing permission to read role bindings");
        }

        if (catalogType != null && !visibleDomains.contains(catalogType)) {
            return ApiFallbacks.error(Response.Status.FORBIDDEN, "Missing permission to read role bindings");
        }

        var bindings = securityService.listRoles(subject, catalogType);

        var filtered = bindings.stream()
                .filter(binding -> visibleDomains.contains(binding.catalogType()))
                .map(this::toApiBinding)
                .toList();

        var response = new GetRolesResponse();
        response.setBindings(filtered);

        return Response.status(Response.Status.OK).entity(response).build();
    }

    @Override
    public Response updateRoles(UpdateRolesRequest updateRolesRequest, SecurityContext securityContext) {
        if (updateRolesRequest == null || updateRolesRequest.getBindings() == null || updateRolesRequest.getBindings().isEmpty()) {
            return ApiFallbacks.error(Response.Status.BAD_REQUEST, "Role bindings are required");
        }

        var subject = securityService.subject(securityContext);
        final List<StoredRoleBinding> bindings;
        try {
            bindings = toStoredBindings(updateRolesRequest.getBindings());
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

        var response = new GetRolesResponse();
        response.setBindings(bindings.stream().map(this::toApiBinding).toList());
        return Response.status(Response.Status.OK).entity(response).build();
    }

    private Set<CatalogType> getReadableDomains(String subject) {
        var result = new java.util.HashSet<CatalogType>();
        for (var type : CatalogType.values()) {
            if (securityService.canSecurityRead(subject, type)) {
                result.add(type);
            }
        }
        return result;
    }

    private List<StoredRoleBinding> toStoredBindings(List<RoleBinding> bindings) {
        var result = new ArrayList<StoredRoleBinding>(bindings.size());
        for (var binding : bindings) {
            if (binding == null || binding.getSubject() == null || binding.getCatalogType() == null || binding.getRole() == null) {
                throw new IllegalArgumentException("Role bindings contain null fields");
            }
            result.add(new StoredRoleBinding(binding.getSubject(), binding.getCatalogType(), binding.getRole()));
        }
        return result;
    }

    private Set<CatalogType> distinctTypes(List<StoredRoleBinding> bindings) {
        return bindings.stream().map(StoredRoleBinding::catalogType).collect(Collectors.toSet());
    }

    private RoleBinding toApiBinding(StoredRoleBinding binding) {
        var roleBinding = new RoleBinding();
        roleBinding.setSubject(binding.subject());
        roleBinding.setCatalogType(binding.catalogType());
        roleBinding.setRole(binding.role());
        return roleBinding;
    }
}
