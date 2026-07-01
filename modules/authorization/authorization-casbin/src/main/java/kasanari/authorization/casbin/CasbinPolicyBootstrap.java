package kasanari.authorization.casbin;

import kasanari.authorization.spi.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class CasbinPolicyBootstrap {
    private final Map<String, List<String>> rolePermissions;

    CasbinPolicyBootstrap() {
        rolePermissions = Map.ofEntries(
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
    }

    List<String> permissionPatternsForRole(String role) {
        var patterns = rolePermissions.get(role);
        if (patterns == null) {
            return List.of();
        }
        return patterns;
    }

    private List<String> roleBindingAdministrationPermissions() {
        return List.of(
                Permission.RoleBindingGet.wireName(),
                Permission.RoleBindingAdd.wireName(),
                Permission.RoleBindingDelete.wireName()
        );
    }

    private List<String> icebergAdminPermissions() {
        var permissions = new ArrayList<String>();
        permissions.addAll(allIcebergPermissions());
        permissions.addAll(roleBindingAdministrationPermissions());
        return List.copyOf(permissions);
    }

    private List<String> icebergEditorPermissions() {
        return List.of(
                Permission.IcebergTableList.wireName(),
                Permission.IcebergTableCreate.wireName(),
                Permission.IcebergTableGet.wireName(),
                Permission.IcebergTableDrop.wireName(),
                Permission.IcebergTableAlter.wireName(),
                Permission.IcebergViewList.wireName(),
                Permission.IcebergViewCreate.wireName(),
                Permission.IcebergViewGet.wireName(),
                Permission.IcebergViewDrop.wireName(),
                Permission.IcebergViewAlter.wireName(),
                Permission.IcebergViewExists.wireName(),
                Permission.IcebergNamespaceList.wireName(),
                Permission.IcebergNamespaceCreate.wireName(),
                Permission.IcebergNamespaceGet.wireName(),
                Permission.IcebergNamespaceDrop.wireName(),
                Permission.IcebergNamespaceAlter.wireName(),
                Permission.IcebergNamespaceExists.wireName(),
                Permission.IcebergTransactionCommit.wireName(),
                Permission.IcebergMetricsReport.wireName()
        );
    }

    private List<String> icebergViewerPermissions() {
        return List.of(
                Permission.IcebergTableList.wireName(),
                Permission.IcebergTableGet.wireName(),
                Permission.IcebergTableExists.wireName(),
                Permission.IcebergViewList.wireName(),
                Permission.IcebergViewGet.wireName(),
                Permission.IcebergViewExists.wireName(),
                Permission.IcebergNamespaceList.wireName(),
                Permission.IcebergNamespaceGet.wireName(),
                Permission.IcebergNamespaceExists.wireName(),
                Permission.IcebergCatalogGet.wireName()
        );
    }

    private List<String> allIcebergPermissions() {
        return List.of(
                Permission.IcebergTableList.wireName(),
                Permission.IcebergTableCreate.wireName(),
                Permission.IcebergTableGet.wireName(),
                Permission.IcebergTableDrop.wireName(),
                Permission.IcebergTableAlter.wireName(),
                Permission.IcebergTableExists.wireName(),
                Permission.IcebergViewList.wireName(),
                Permission.IcebergViewCreate.wireName(),
                Permission.IcebergViewGet.wireName(),
                Permission.IcebergViewDrop.wireName(),
                Permission.IcebergViewAlter.wireName(),
                Permission.IcebergViewExists.wireName(),
                Permission.IcebergNamespaceList.wireName(),
                Permission.IcebergNamespaceCreate.wireName(),
                Permission.IcebergNamespaceGet.wireName(),
                Permission.IcebergNamespaceDrop.wireName(),
                Permission.IcebergNamespaceAlter.wireName(),
                Permission.IcebergNamespaceExists.wireName(),
                Permission.IcebergTransactionCommit.wireName(),
                Permission.IcebergMetricsReport.wireName(),
                Permission.IcebergCatalogCreate.wireName(),
                Permission.IcebergCatalogGet.wireName(),
                Permission.IcebergCatalogUpdate.wireName(),
                Permission.IcebergCatalogDelete.wireName()
        );
    }

    private List<String> paimonAdminPermissions() {
        var permissions = new ArrayList<String>();
        permissions.addAll(allPaimonPermissions());
        permissions.addAll(roleBindingAdministrationPermissions());
        return List.copyOf(permissions);
    }

    private List<String> paimonEditorPermissions() {
        return List.of(
                Permission.PaimonDatabaseList.wireName(),
                Permission.PaimonDatabaseCreate.wireName(),
                Permission.PaimonDatabaseGet.wireName(),
                Permission.PaimonDatabaseDrop.wireName(),
                Permission.PaimonDatabaseAlter.wireName(),
                Permission.PaimonTableList.wireName(),
                Permission.PaimonTableCreate.wireName(),
                Permission.PaimonTableGet.wireName(),
                Permission.PaimonTableDrop.wireName(),
                Permission.PaimonTableAlter.wireName(),
                Permission.PaimonTableExists.wireName(),
                Permission.PaimonViewList.wireName(),
                Permission.PaimonViewCreate.wireName(),
                Permission.PaimonViewGet.wireName(),
                Permission.PaimonViewDrop.wireName(),
                Permission.PaimonViewAlter.wireName(),
                Permission.PaimonFunctionList.wireName(),
                Permission.PaimonFunctionCreate.wireName(),
                Permission.PaimonFunctionGet.wireName(),
                Permission.PaimonFunctionDrop.wireName(),
                Permission.PaimonFunctionAlter.wireName(),
                Permission.PaimonBranchList.wireName(),
                Permission.PaimonBranchCreate.wireName(),
                Permission.PaimonBranchDrop.wireName(),
                Permission.PaimonBranchAlter.wireName(),
                Permission.PaimonPartitionList.wireName(),
                Permission.PaimonPartitionAlter.wireName(),
                Permission.PaimonTagList.wireName(),
                Permission.PaimonTagCreate.wireName(),
                Permission.PaimonTagGet.wireName(),
                Permission.PaimonTagDrop.wireName(),
                Permission.PaimonConfigGet.wireName()
        );
    }

    private List<String> paimonViewerPermissions() {
        return List.of(
                Permission.PaimonDatabaseList.wireName(),
                Permission.PaimonTableList.wireName(),
                Permission.PaimonViewList.wireName(),
                Permission.PaimonFunctionList.wireName(),
                Permission.PaimonBranchList.wireName(),
                Permission.PaimonPartitionList.wireName(),
                Permission.PaimonTagList.wireName(),
                Permission.PaimonDatabaseGet.wireName(),
                Permission.PaimonTableGet.wireName(),
                Permission.PaimonViewGet.wireName(),
                Permission.PaimonFunctionGet.wireName(),
                Permission.PaimonConfigGet.wireName(),
                Permission.PaimonTagGet.wireName(),
                Permission.PaimonTableExists.wireName()
        );
    }

    private List<String> allPaimonPermissions() {
        return List.of(
                Permission.PaimonDatabaseList.wireName(),
                Permission.PaimonDatabaseCreate.wireName(),
                Permission.PaimonDatabaseGet.wireName(),
                Permission.PaimonDatabaseDrop.wireName(),
                Permission.PaimonDatabaseAlter.wireName(),
                Permission.PaimonTableList.wireName(),
                Permission.PaimonTableCreate.wireName(),
                Permission.PaimonTableGet.wireName(),
                Permission.PaimonTableDrop.wireName(),
                Permission.PaimonTableAlter.wireName(),
                Permission.PaimonTableExists.wireName(),
                Permission.PaimonViewList.wireName(),
                Permission.PaimonViewCreate.wireName(),
                Permission.PaimonViewGet.wireName(),
                Permission.PaimonViewDrop.wireName(),
                Permission.PaimonViewAlter.wireName(),
                Permission.PaimonFunctionList.wireName(),
                Permission.PaimonFunctionCreate.wireName(),
                Permission.PaimonFunctionGet.wireName(),
                Permission.PaimonFunctionDrop.wireName(),
                Permission.PaimonFunctionAlter.wireName(),
                Permission.PaimonBranchList.wireName(),
                Permission.PaimonBranchCreate.wireName(),
                Permission.PaimonBranchDrop.wireName(),
                Permission.PaimonBranchAlter.wireName(),
                Permission.PaimonPartitionList.wireName(),
                Permission.PaimonPartitionAlter.wireName(),
                Permission.PaimonTagList.wireName(),
                Permission.PaimonTagCreate.wireName(),
                Permission.PaimonTagGet.wireName(),
                Permission.PaimonTagDrop.wireName(),
                Permission.PaimonConfigGet.wireName(),
                Permission.PaimonCatalogCreate.wireName(),
                Permission.PaimonCatalogGet.wireName(),
                Permission.PaimonCatalogUpdate.wireName(),
                Permission.PaimonCatalogDelete.wireName()
        );
    }

    private List<String> lanceAdminPermissions() {
        var permissions = new ArrayList<String>();
        permissions.addAll(allLancePermissions());
        permissions.addAll(roleBindingAdministrationPermissions());
        return List.copyOf(permissions);
    }

    private List<String> lanceEditorPermissions() {
        return List.of(
                Permission.LanceNamespaceList.wireName(),
                Permission.LanceNamespaceCreate.wireName(),
                Permission.LanceNamespaceGet.wireName(),
                Permission.LanceNamespaceDrop.wireName(),
                Permission.LanceNamespaceAlter.wireName(),
                Permission.LanceNamespaceExists.wireName(),
                Permission.LanceTableList.wireName(),
                Permission.LanceTableCreate.wireName(),
                Permission.LanceTableGet.wireName(),
                Permission.LanceTableDrop.wireName(),
                Permission.LanceTableAlter.wireName(),
                Permission.LanceTableExists.wireName()
        );
    }

    private List<String> lanceViewerPermissions() {
        return List.of(
                Permission.LanceNamespaceList.wireName(),
                Permission.LanceTableList.wireName(),
                Permission.LanceNamespaceGet.wireName(),
                Permission.LanceTableGet.wireName(),
                Permission.LanceCatalogGet.wireName(),
                Permission.LanceNamespaceExists.wireName(),
                Permission.LanceTableExists.wireName()
        );
    }

    private List<String> allLancePermissions() {
        return List.of(
                Permission.LanceNamespaceList.wireName(),
                Permission.LanceNamespaceCreate.wireName(),
                Permission.LanceNamespaceGet.wireName(),
                Permission.LanceNamespaceDrop.wireName(),
                Permission.LanceNamespaceAlter.wireName(),
                Permission.LanceNamespaceExists.wireName(),
                Permission.LanceTableList.wireName(),
                Permission.LanceTableCreate.wireName(),
                Permission.LanceTableGet.wireName(),
                Permission.LanceTableDrop.wireName(),
                Permission.LanceTableAlter.wireName(),
                Permission.LanceTableExists.wireName(),
                Permission.LanceCatalogCreate.wireName(),
                Permission.LanceCatalogGet.wireName(),
                Permission.LanceCatalogUpdate.wireName(),
                Permission.LanceCatalogDelete.wireName()
        );
    }
}
