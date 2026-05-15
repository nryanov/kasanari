package kasanari.server.infrastructure.management;

import kasanari.catalog.management.dto.CatalogTypeDto;
import kasanari.catalog.management.dto.RoleBindingDto;
import kasanari.repository.management.security.model.Role;
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
                    toDomain(binding.getRole())
            ));
        }
        return result;
    }

    public static RoleBindingDto toApi(StoredRoleBinding binding) {
        var roleBinding = new RoleBindingDto();
        roleBinding.setSubject(binding.subject());
        roleBinding.setCatalogType(toApi(binding.catalogType()));
        roleBinding.setRole(toApi(binding.role()));
        return roleBinding;
    }

    public static kasanari.repository.management.common.model.CatalogType toDomain(CatalogTypeDto type) {
        return switch (type) {
            case ICEBERG -> kasanari.repository.management.common.model.CatalogType.ICEBERG;
            case PAIMON -> kasanari.repository.management.common.model.CatalogType.PAIMON;
            case LANCE -> kasanari.repository.management.common.model.CatalogType.LANCE;
        };
    }

    public static CatalogTypeDto toApi(kasanari.repository.management.common.model.CatalogType type) {
        return switch (type) {
            case ICEBERG -> CatalogTypeDto.ICEBERG;
            case PAIMON -> CatalogTypeDto.PAIMON;
            case LANCE -> CatalogTypeDto.LANCE;
        };
    }

    private static Role toDomain(RoleBindingDto.RoleEnum role) {
        return switch (role) {
            case CATALOG_ADMIN -> Role.CATALOG_ADMIN;
            case CATALOG_READER -> Role.CATALOG_READER;
            case SECURITY_ADMIN -> Role.SECURITY_ADMIN;
            case SECURITY_READER -> Role.SECURITY_READER;
        };
    }

    private static RoleBindingDto.RoleEnum toApi(Role role) {
        return switch (role) {
            case CATALOG_ADMIN -> RoleBindingDto.RoleEnum.CATALOG_ADMIN;
            case CATALOG_READER -> RoleBindingDto.RoleEnum.CATALOG_READER;
            case SECURITY_ADMIN -> RoleBindingDto.RoleEnum.SECURITY_ADMIN;
            case SECURITY_READER -> RoleBindingDto.RoleEnum.SECURITY_READER;
        };
    }
}
