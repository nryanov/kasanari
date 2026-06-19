package kasanari.server.infrastructure.management;

import kasanari.authorization.spi.AuthorizationResource;
import kasanari.authorization.spi.RoleBinding;
import kasanari.catalog.management.dto.RoleBindingDto;
import kasanari.repository.management.security.model.StoredRoleBinding;

import java.util.ArrayList;
import java.util.List;

public final class RoleBindingMapper {
    private RoleBindingMapper() {
    }

    public static List<StoredRoleBinding> toDomain(List<RoleBindingDto> bindings) {
        var result = new ArrayList<StoredRoleBinding>(bindings.size());
        for (var binding : bindings) {
            result.add(toStored(binding));
        }
        return result;
    }

    public static List<RoleBinding> toSpi(List<RoleBindingDto> bindings) {
        var result = new ArrayList<RoleBinding>(bindings.size());
        for (var binding : bindings) {
            result.add(toSpi(binding));
        }
        return result;
    }

    public static RoleBindingDto toApi(StoredRoleBinding binding) {
        var roleBinding = new RoleBindingDto();
        roleBinding.setSubject(binding.subject());
        roleBinding.setRole(binding.role());
        roleBinding.setResource(binding.resource());
        return roleBinding;
    }

    public static RoleBindingDto toApi(RoleBinding binding) {
        var roleBinding = new RoleBindingDto();
        roleBinding.setSubject(binding.subject());
        roleBinding.setRole(binding.role());
        roleBinding.setResource(binding.resource());
        return roleBinding;
    }

    private static RoleBinding toSpi(RoleBindingDto binding) {
        validateBinding(binding);
        return new RoleBinding(binding.getSubject(), binding.getRole(), binding.getResource());
    }

    private static StoredRoleBinding toStored(RoleBindingDto binding) {
        validateBinding(binding);
        return new StoredRoleBinding(binding.getSubject(), binding.getRole(), binding.getResource());
    }

    private static void validateBinding(RoleBindingDto binding) {
        if (binding == null || binding.getSubject() == null || binding.getRole() == null || binding.getResource() == null) {
            throw new IllegalArgumentException("Role bindings must include subject, role, and resource");
        }
        AuthorizationResource.parse(binding.getResource());
    }
}
