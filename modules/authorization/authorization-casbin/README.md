# authorization-casbin

Default Kasanari authorization provider using in-memory Casbin RBAC with role bindings stored in PostgreSQL.

## Enable

```properties
kasanari.authorization.type=casbin
```

JDBC settings are read from `kasanari.authorization.casbin.*` or, if unset, from `management.metadata.jdbc-properties.*`.

Optional superusers (always allowed):

```properties
kasanari.authorization.casbin.superuser-subjects=root,admin
```

## Default roles

Roles are assigned per subject and catalog type (`ICEBERG`, `PAIMON`, `LANCE`) via `PUT /management/v1/security/roles`.

| Role                                       | Scope                                                                 |
|--------------------------------------------|-----------------------------------------------------------------------|
| `IcebergCatalogAdmin`                      | All `Iceberg*` permissions plus `RoleSelect`, `RoleAdd`, `RoleRemove` |
| `IcebergCatalogEditor`                     | Iceberg table/namespace/view mutations and reads                      |
| `IcebergCatalogViewer`                     | Iceberg `*List`, `*Get`, `*Exists`                                    |
| `PaimonCatalogAdmin` / `Editor` / `Viewer` | Same pattern with `Paimon*` permissions                               |
| `LanceCatalogAdmin` / `Editor` / `Viewer`  | Same pattern with `Lance*` permissions                                |

Permission names are defined in `authorization-spi` (`Permission` enum), e.g. `IcebergTableList`, `PaimonDatabaseCreate`, `RoleSelect`.

## Migration from legacy roles

If upgrading from the previous coarse roles, update stored bindings:

```sql
UPDATE kasanari_role_bindings SET role_name = 'IcebergCatalogAdmin' WHERE role_name = 'catalog_admin';
UPDATE kasanari_role_bindings SET role_name = 'IcebergCatalogViewer' WHERE role_name = 'catalog_reader';
-- Repeat per catalog type and role as needed
```

Legacy names `security_admin` / `security_reader` map to catalog admin/viewer roles that include `Role*` permissions.
