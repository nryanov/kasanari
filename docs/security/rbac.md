# RBAC Roles and Permissions

Kasanari RBAC is available when:

```properties
kasanari.authorization.type=casbin
```

Role bindings are stored and managed through Management API:

- `GET /management/v1/security/roles`
- `PUT /management/v1/security/roles`
- `DELETE /management/v1/security/roles`

Bindings are scoped by `catalogType` (`ICEBERG`, `PAIMON`, `LANCE`).

## Default Casbin roles

| Catalog type | Admin role | Editor role | Viewer role |
|---|---|---|---|
| ICEBERG | `IcebergCatalogAdmin` | `IcebergCatalogEditor` | `IcebergCatalogViewer` |
| PAIMON | `PaimonCatalogAdmin` | `PaimonCatalogEditor` | `PaimonCatalogViewer` |
| LANCE | `LanceCatalogAdmin` | `LanceCatalogEditor` | `LanceCatalogViewer` |

## Role behavior

| Role level | Permissions behavior |
|---|---|
| `*CatalogAdmin` | All permissions with catalog prefix (`Iceberg*`, `Paimon*`, `Lance*`) plus `RoleSelect`, `RoleAdd`, `RoleRemove`. |
| `*CatalogEditor` | Mutation + read for catalog objects (engine-specific wildcard policy set). |
| `*CatalogViewer` | Read-only pattern (`*List`, `*Get`, `*Exists`) for that catalog prefix. |

## Available permissions

All permissions are defined in `kasanari.authorization.spi.Permission`.

### Iceberg permissions

- `IcebergTableList`
- `IcebergTableCreate`
- `IcebergTableGet`
- `IcebergTableDrop`
- `IcebergTableAlter`
- `IcebergViewList`
- `IcebergViewCreate`
- `IcebergViewGet`
- `IcebergViewDrop`
- `IcebergViewAlter`
- `IcebergNamespaceList`
- `IcebergNamespaceCreate`
- `IcebergNamespaceGet`
- `IcebergNamespaceDrop`
- `IcebergNamespaceAlter`
- `IcebergNamespaceExists`
- `IcebergTransactionCommit`
- `IcebergMetricsReport`
- `IcebergTableExists`
- `IcebergViewExists`
- `IcebergCatalogCreate`
- `IcebergCatalogGet`
- `IcebergCatalogUpdate`
- `IcebergCatalogDelete`

### Paimon permissions

- `PaimonDatabaseList`
- `PaimonDatabaseCreate`
- `PaimonDatabaseGet`
- `PaimonDatabaseDrop`
- `PaimonDatabaseAlter`
- `PaimonTableList`
- `PaimonTableCreate`
- `PaimonTableGet`
- `PaimonTableDrop`
- `PaimonTableAlter`
- `PaimonTableExists`
- `PaimonViewList`
- `PaimonViewCreate`
- `PaimonViewGet`
- `PaimonViewDrop`
- `PaimonViewAlter`
- `PaimonFunctionList`
- `PaimonFunctionCreate`
- `PaimonFunctionGet`
- `PaimonFunctionDrop`
- `PaimonFunctionAlter`
- `PaimonBranchList`
- `PaimonBranchCreate`
- `PaimonBranchDrop`
- `PaimonBranchAlter`
- `PaimonPartitionList`
- `PaimonPartitionAlter`
- `PaimonTagList`
- `PaimonTagCreate`
- `PaimonTagGet`
- `PaimonTagDrop`
- `PaimonConfigGet`
- `PaimonCatalogCreate`
- `PaimonCatalogGet`
- `PaimonCatalogUpdate`
- `PaimonCatalogDelete`

### Lance permissions

- `LanceNamespaceList`
- `LanceNamespaceCreate`
- `LanceNamespaceGet`
- `LanceNamespaceDrop`
- `LanceNamespaceAlter`
- `LanceNamespaceExists`
- `LanceTableList`
- `LanceTableCreate`
- `LanceTableGet`
- `LanceTableDrop`
- `LanceTableAlter`
- `LanceTableExists`
- `LanceCatalogCreate`
- `LanceCatalogGet`
- `LanceCatalogUpdate`
- `LanceCatalogDelete`

### Role administration permissions

- `RoleSelect`
- `RoleAdd`
- `RoleRemove`

## Example role binding payload

```json
{
  "bindings": [
    {
      "subject": "alice",
      "catalogType": "ICEBERG",
      "role": "IcebergCatalogViewer"
    },
    {
      "subject": "platform-admin",
      "catalogType": "PAIMON",
      "role": "PaimonCatalogAdmin"
    }
  ]
}
```

## Notes

- Role bindings are currently scoped by `catalogType` (not by individual `catalogId`).
- Superusers configured in `kasanari.authorization.casbin.superuser-subjects` bypass checks.
