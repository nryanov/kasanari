# authorization-casbin

Default Kasanari authorization provider using in-memory Casbin ACL with role bindings stored in PostgreSQL.

## Enable

```properties
kasanari.authorization.type=casbin
```

JDBC settings are read from `kasanari.authorization.casbin.*`:

```properties
kasanari.authorization.casbin.jdbc.uri=jdbc:postgresql://localhost:5432/kasanari
kasanari.authorization.casbin.jdbc.user=kasanari
kasanari.authorization.casbin.jdbc.password=secret
```

Optional superusers (always allowed):

```properties
kasanari.authorization.casbin.superuser-subjects=root,admin
```

## Role bindings

Bindings are `(subject, role, resource)` where `resource` is a fully qualified scope pattern:

- Catalog: `ICEBERG/warehouse/*`
- Namespace: `PAIMON/events/ns1/*` (Paimon databases use the `namespace` segment)
- Leaf object: `LANCE/lake/ns1/users` (exact path without `/*`)

Roles are managed via `PUT /management/v1/security/roles`. At reload time each binding is expanded into flat Casbin policies `(subject, resourceScope, permissionPattern)`.

## Default roles

| Role                                       | Scope                                                                 |
|--------------------------------------------|-----------------------------------------------------------------------|
| `IcebergCatalogAdmin`                      | All `Iceberg*` permissions plus `RoleSelect`, `RoleAdd`, `RoleRemove` |
| `IcebergCatalogEditor`                     | Iceberg table/namespace/view mutations and reads                      |
| `IcebergCatalogViewer`                     | Iceberg `*List`, `*Get`, `*Exists`                                    |
| `PaimonCatalogAdmin` / `Editor` / `Viewer` | Same pattern with `Paimon*` permissions                               |
| `LanceCatalogAdmin` / `Editor` / `Viewer`  | Same pattern with `Lance*` permissions                                |

Permission names are defined in `authorization-spi` (`Permission` enum), e.g. `IcebergTableList`, `PaimonDatabaseCreate`, `RoleSelect`.

## Casbin model

```
r = sub, obj, perm
p = sub, obj, perm
m = r.sub == p.sub && keyMatch3(r.obj, p.obj) && globMatch(r.perm, p.perm)
```

Inheritance is handled by `keyMatch3`: a binding at `ICEBERG/prod/*` grants access to `ICEBERG/prod/analytics/orders`.
