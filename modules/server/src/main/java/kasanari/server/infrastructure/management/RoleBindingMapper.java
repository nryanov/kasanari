package kasanari.server.infrastructure.management;

import kasanari.authorization.spi.RoleBinding;
import kasanari.catalog.management.dto.RoleBindingDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RoleBindingMapper {
    private RoleBindingMapper() {
    }

    public static List<RoleBinding> toSpi(List<RoleBindingDto> bindings) {
        var result = new ArrayList<RoleBinding>(bindings.size());
        for (var binding : bindings) {
            result.add(toSpi(binding));
        }
        return result;
    }

    public static RoleBindingDto toApi(RoleBinding binding) {
        var roleBinding = new RoleBindingDto();
        roleBinding.setSubject(binding.subject());
        roleBinding.setRole(binding.role());
        roleBinding.setResource(binding.resource());
        roleBinding.setEffect(binding.effect());
        return roleBinding;
    }

    private static RoleBinding toSpi(RoleBindingDto binding) {
        validateBinding(binding);
        return new RoleBinding(
                binding.getSubject(),
                binding.getResource(),
                binding.getRole(),
                normalizedEffect(binding.getEffect())
        );
    }

    private static void validateBinding(RoleBindingDto binding) {
        if (binding == null
                || binding.getSubject() == null
                || binding.getRole() == null
                || binding.getResource() == null
                || binding.getEffect() == null) {
            throw new IllegalArgumentException("Role bindings must include subject, role, resource, and effect");
        }
        if (binding.getSubject().isBlank()
                || binding.getRole().isBlank()
                || binding.getResource().isBlank()
                || binding.getEffect().isBlank()) {
            throw new IllegalArgumentException("Role bindings must not contain blank fields");
        }
    }

    private static String normalizedEffect(String effect) {
        var normalized = effect.toLowerCase(Locale.ROOT);
        if (!"allow".equals(normalized) && !"deny".equals(normalized)) {
            throw new IllegalArgumentException("Role binding effect must be either 'allow' or 'deny'");
        }
        return normalized;
    }
}
