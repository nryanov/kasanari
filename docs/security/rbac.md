# RBAC Roles and Permissions

Kasanari RBAC is available when `casbin` authorization is used:

```properties
kasanari.authorization.type=casbin
```

Another way to use it is to implement custom authorization logic via SPI `AuthorizationProvider`. 

Role bindings are stored and managed through Management API:

- `GET /management/v1/security/roles`
- `PUT /management/v1/security/roles`
- `DELETE /management/v1/security/roles`

Bindings are scoped by fully qualified resource patterns. The catalog engine (`ICEBERG`, `PAIMON`, `LANCE`) is the first path segment.

## Resource path format

| Level | Path pattern | Example |
|-------|--------------|---------|
| Catalog | `{type}/{catalogName}/*` | `ICEBERG/warehouse/*` |
| Namespace | `{type}/{catalogName}/{namespace}/*` | `PAIMON/events/ns1/*` |
| Table/View | `{type}/{catalogName}/{namespace}/{name}` | `LANCE/lake/ns1/users` |

- Segment delimiter: `/`
- Iceberg multi-level namespaces stay dot-encoded in one segment (for example `ns1.ns2`)
- Wildcard suffix `/*` denotes this level and descendants
- Paimon databases map to the `namespace` segment in resource paths

## Default Casbin roles

| Catalog type | Admin role            | Editor role            | Viewer role            |
|--------------|-----------------------|------------------------|------------------------|
| ICEBERG      | `IcebergCatalogAdmin` | `IcebergCatalogEditor` | `IcebergCatalogViewer` |
| PAIMON       | `PaimonCatalogAdmin`  | `PaimonCatalogEditor`  | `PaimonCatalogViewer`  |
| LANCE        | `LanceCatalogAdmin`   | `LanceCatalogEditor`   | `LanceCatalogViewer`   |

## Role behavior

| Role level       | Permissions behavior                                                                                              |
|------------------|-------------------------------------------------------------------------------------------------------------------|
| `*CatalogAdmin`  | All permissions with catalog prefix (`Iceberg*`, `Paimon*`, `Lance*`) plus `RoleSelect`, `RoleAdd`, `RoleRemove`. |
| `*CatalogEditor` | Mutation + read for catalog objects (engine-specific wildcard policy set).                                        |
| `*CatalogViewer` | Read-only pattern (`*List`, `*Get`, `*Exists`) for that catalog prefix.                                           |

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
      "role": "IcebergCatalogViewer",
      "resource": "ICEBERG/warehouse/analytics/*"
    },
    {
      "subject": "platform-admin",
      "role": "PaimonCatalogAdmin",
      "resource": "PAIMON/events/*"
    }
  ]
}
```

List bindings with optional `resourcePrefix` query parameter (for example `ICEBERG/` or `ICEBERG/warehouse/`).

## Notes

- Role bindings require an explicit `resource` scope pattern.
- Superusers configured in `kasanari.authorization.casbin.superuser-subjects` bypass checks.
- Namespace-scoped bindings inherit access to tables and views under that namespace.
