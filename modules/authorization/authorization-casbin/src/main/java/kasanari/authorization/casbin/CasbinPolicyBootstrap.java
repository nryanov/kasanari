package kasanari.authorization.casbin;

import kasanari.authorization.spi.Permission;
import kasanari.core.model.CatalogType;

import java.util.List;
import java.util.Map;

final class CasbinPolicyBootstrap {
    private static final Map<String, List<String>> ROLE_PERMISSIONS = Map.ofEntries(
            Map.entry(CasbinRoles.ICEBERG_CATALOG_ADMIN, icebergAdminPermissions()),
            Map.entry(CasbinRoles.ICEBERG_CATALOG_EDITOR, icebergEditorPermissions()),
            Map.entry(CasbinRoles.ICEBERG_CATALOG_VIEWER, icebergViewerPermissions()),
            Map.entry(CasbinRoles.PAIMON_CATALOG_ADMIN, paimonAdminPermissions()),
            Map.entry(CasbinRoles.PAIMON_CATALOG_EDITOR, paimonEditorPermissions()),
            Map.entry(CasbinRoles.PAIMON_CATALOG_VIEWER, paimonViewerPermissions()),
            Map.entry(CasbinRoles.LANCE_CATALOG_ADMIN, lanceAdminPermissions()),
            Map.entry(CasbinRoles.LANCE_CATALOG_EDITOR, lanceEditorPermissions()),
            Map.entry(CasbinRoles.LANCE_CATALOG_VIEWER, lanceViewerPermissions())
    );

    private CasbinPolicyBootstrap() {
    }

    static List<String> permissionPatternsForRole(String role) {
        var patterns = ROLE_PERMISSIONS.get(role);
        if (patterns == null) {
            return List.of();
        }
        return patterns;
    }

    private static List<String> icebergAdminPermissions() {
        return List.of(
                "Iceberg*",
                Permission.RoleSelect.wireName(),
                Permission.RoleAdd.wireName(),
                Permission.RoleRemove.wireName()
        );
    }

    private static List<String> icebergEditorPermissions() {
        return List.of(
                Permission.IcebergTableList.wireName(),
                Permission.IcebergTableCreate.wireName(),
                Permission.IcebergTableGet.wireName(),
                Permission.IcebergTableDrop.wireName(),
                Permission.IcebergTableAlter.wireName(),
                "IcebergNamespace*",
                "IcebergView*",
                Permission.IcebergTransactionCommit.wireName(),
                Permission.IcebergMetricsReport.wireName()
        );
    }

    private static List<String> icebergViewerPermissions() {
        return List.of("Iceberg*List", "Iceberg*Get", "Iceberg*Exists");
    }

    private static List<String> paimonAdminPermissions() {
        return List.of(
                "Paimon*",
                Permission.RoleSelect.wireName(),
                Permission.RoleAdd.wireName(),
                Permission.RoleRemove.wireName()
        );
    }

    private static List<String> paimonEditorPermissions() {
        return List.of(
                "PaimonDatabase*",
                "PaimonTable*",
                "PaimonView*",
                "PaimonFunction*",
                "PaimonBranch*",
                "PaimonPartition*",
                "PaimonTag*",
                Permission.PaimonConfigGet.wireName()
        );
    }

    private static List<String> paimonViewerPermissions() {
        return List.of("Paimon*List", "Paimon*Get", "Paimon*Exists");
    }

    private static List<String> lanceAdminPermissions() {
        return List.of(
                "Lance*",
                Permission.RoleSelect.wireName(),
                Permission.RoleAdd.wireName(),
                Permission.RoleRemove.wireName()
        );
    }

    private static List<String> lanceEditorPermissions() {
        return List.of("LanceNamespace*", "LanceTable*");
    }

    private static List<String> lanceViewerPermissions() {
        return List.of("Lance*List", "Lance*Get", "Lance*Exists");
    }

    static String catalogTypePrefix(CatalogType catalogType) {
        return switch (catalogType) {
            case ICEBERG -> "Iceberg";
            case PAIMON -> "Paimon";
            case LANCE -> "Lance";
        };
    }
}
