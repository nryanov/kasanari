package kasanari.authorization.casbin;

import kasanari.authorization.spi.Permission;
import kasanari.repository.management.common.model.CatalogType;
import org.casbin.jcasbin.main.Enforcer;

import java.util.List;

final class CasbinPolicyBootstrap {
    private CasbinPolicyBootstrap() {
    }

    static void initRolePermissions(Enforcer enforcer) {
        for (var domain : CatalogType.values()) {
            var prefix = switch (domain) {
                case ICEBERG -> "Iceberg";
                case PAIMON -> "Paimon";
                case LANCE -> "Lance";
            };
            var adminRole = switch (domain) {
                case ICEBERG -> CasbinRoles.ICEBERG_CATALOG_ADMIN;
                case PAIMON -> CasbinRoles.PAIMON_CATALOG_ADMIN;
                case LANCE -> CasbinRoles.LANCE_CATALOG_ADMIN;
            };
            var editorRole = switch (domain) {
                case ICEBERG -> CasbinRoles.ICEBERG_CATALOG_EDITOR;
                case PAIMON -> CasbinRoles.PAIMON_CATALOG_EDITOR;
                case LANCE -> CasbinRoles.LANCE_CATALOG_EDITOR;
            };
            var viewerRole = switch (domain) {
                case ICEBERG -> CasbinRoles.ICEBERG_CATALOG_VIEWER;
                case PAIMON -> CasbinRoles.PAIMON_CATALOG_VIEWER;
                case LANCE -> CasbinRoles.LANCE_CATALOG_VIEWER;
            };
            var domainName = domain.toString();

            enforcer.addPolicy(adminRole, domainName, prefix + "*");
            enforcer.addPolicy(adminRole, domainName, Permission.RoleSelect.wireName());
            enforcer.addPolicy(adminRole, domainName, Permission.RoleAdd.wireName());
            enforcer.addPolicy(adminRole, domainName, Permission.RoleRemove.wireName());

            for (var permission : editorPermissions(prefix, domain)) {
                enforcer.addPolicy(editorRole, domainName, permission);
            }

            enforcer.addPolicy(viewerRole, domainName, prefix + "*List");
            enforcer.addPolicy(viewerRole, domainName, prefix + "*Get");
            enforcer.addPolicy(viewerRole, domainName, prefix + "*Exists");
        }
    }

    private static List<String> editorPermissions(String prefix, CatalogType domain) {
        if (domain == CatalogType.ICEBERG) {
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
        if (domain == CatalogType.PAIMON) {
            return List.of(
                    prefix + "Database*",
                    prefix + "Table*",
                    prefix + "View*",
                    prefix + "Function*",
                    prefix + "Branch*",
                    prefix + "Partition*",
                    prefix + "Tag*",
                    Permission.PaimonConfigGet.wireName()
            );
        }
        return List.of(
                prefix + "Namespace*",
                prefix + "Table*"
        );
    }
}
