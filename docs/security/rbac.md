# RBAC Roles and Permissions

Kasanari RBAC is available when `casbin` authorization is used:

```properties
kasanari.authorization.type=casbin
```

Another way to use it is to implement custom authorization logic via SPI `AuthorizationProvider`. 

Role bindings are stored and managed through Management API:

- `GET /management/v1/security/roles`
- `POST /management/v1/security/roles`
- `DELETE /management/v1/security/roles`

Bindings are scoped by fully qualified resource paths. The catalog engine (`iceberg`, `paimon`, `lance`) is the first path segment. **No wildcards** — hierarchy is expressed by path depth only.

## Resource path format

| Level | Example path | Grants access to |
|-------|--------------|------------------|
| Engine | `iceberg` | All Iceberg catalogs, namespaces, tables, and views |
| Catalog | `iceberg/warehouse` | Everything under that catalog |
| Namespace | `paimon/events/ns1` | All tables/views in that namespace (Paimon databases use the `namespace` segment) |
| Table/View | `lance/lake/ns1/users` | That object only |

- Segment delimiter: `/`
- Iceberg multi-level namespaces stay dot-encoded in one segment (for example `ns1.ns2`)
- No `*` segment and no `/*` suffix — a binding at `iceberg/warehouse` applies to `iceberg/warehouse/analytics/orders` via prefix inheritance in Casbin

## Default Casbin roles

| Catalog type | Admin role            | Editor role            | Viewer role            |
|--------------|-----------------------|------------------------|------------------------|
| ICEBERG      | `IcebergCatalogAdmin` | `IcebergCatalogEditor` | `IcebergCatalogViewer` |
| PAIMON       | `PaimonCatalogAdmin`  | `PaimonCatalogEditor`  | `PaimonCatalogViewer`  |
| LANCE        | `LanceCatalogAdmin`   | `LanceCatalogEditor`   | `LanceCatalogViewer`   |

## Role behavior

| Role level       | Permissions behavior                                                                                              |
|------------------|-------------------------------------------------------------------------------------------------------------------|
| `*CatalogAdmin`  | All permissions with catalog prefix (`Iceberg*`, `Paimon*`, `Lance*`) plus `RoleBindingGet`, `RoleBindingAdd`, `RoleBindingDelete`. |
| `*CatalogEditor` | Mutation + read for catalog objects (engine-specific policy set). No role-binding administration.                 |
| `*CatalogViewer` | Read-only pattern (`*List`, `*Get`, `*Exists`) for that catalog prefix.                                         |

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

Only `*CatalogAdmin` roles receive these permissions:

- `RoleBindingGet`
- `RoleBindingAdd`
- `RoleBindingDelete`

## Example role binding payload

```json
{
  "bindings": [
    {
      "subject": "alice",
      "role": "IcebergCatalogViewer",
      "resource": "iceberg/warehouse/analytics"
    },
    {
      "subject": "platform-admin",
      "role": "PaimonCatalogAdmin",
      "resource": "paimon/events"
    }
  ]
}
```

List bindings with required `resource` query parameter (exact match) and optional `subject` filter.

## Notes

- Role bindings require an explicit `resource` scope path.
- Superusers configured in `kasanari.authorization.casbin.superuser-subjects` bypass checks.
- Namespace-scoped bindings inherit access to tables and views under that namespace via prefix matching.
- Changing a role on the same scope requires delete + add (primary key is `(subject, resource, role)`).
