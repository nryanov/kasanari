package kasanari.server.infrastructure.management;

import kasanari.authorization.spi.RoleBinding;
import kasanari.catalog.management.dto.CatalogTypeDto;
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
            if (binding == null || binding.getSubject() == null || binding.getCatalogType() == null || binding.getRole() == null) {
                throw new IllegalArgumentException("Role bindings contain null fields");
            }
            result.add(new StoredRoleBinding(
                    binding.getSubject(),
                    toDomain(binding.getCatalogType()),
                    binding.getRole()
            ));
        }
        return result;
    }

    public static List<RoleBinding> toSpi(List<RoleBindingDto> bindings) {
        var result = new ArrayList<RoleBinding>(bindings.size());
        for (var binding : bindings) {
            if (binding == null || binding.getSubject() == null || binding.getCatalogType() == null || binding.getRole() == null) {
                throw new IllegalArgumentException("Role bindings contain null fields");
            }
            result.add(new RoleBinding(
                    binding.getSubject(),
                    toDomain(binding.getCatalogType()),
                    binding.getRole()
            ));
        }
        return result;
    }

    public static RoleBindingDto toApi(StoredRoleBinding binding) {
        var roleBinding = new RoleBindingDto();
        roleBinding.setSubject(binding.subject());
        roleBinding.setCatalogType(toApi(binding.catalogType()));
        roleBinding.setRole(binding.role());
        return roleBinding;
    }

    public static RoleBindingDto toApi(RoleBinding binding) {
        var roleBinding = new RoleBindingDto();
        roleBinding.setSubject(binding.subject());
        roleBinding.setCatalogType(toApi(binding.catalogType()));
        roleBinding.setRole(binding.role());
        return roleBinding;
    }

    public static kasanari.core.model.CatalogType toDomain(CatalogTypeDto type) {
        return switch (type) {
            case ICEBERG -> kasanari.core.model.CatalogType.ICEBERG;
            case PAIMON -> kasanari.core.model.CatalogType.PAIMON;
            case LANCE -> kasanari.core.model.CatalogType.LANCE;
        };
    }

    public static CatalogTypeDto toApi(kasanari.core.model.CatalogType type) {
        return switch (type) {
            case ICEBERG -> CatalogTypeDto.ICEBERG;
            case PAIMON -> CatalogTypeDto.PAIMON;
            case LANCE -> CatalogTypeDto.LANCE;
        };
    }
}
