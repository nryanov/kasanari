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

Policy reload interval (all instances poll DB revision on this schedule; default `5m`):

```properties
kasanari.authorization.casbin.refresh-interval=5m
```

## Role bindings

Bindings are `(subject, resource, role)` where `resource` is a fully qualified scope path (no wildcards):

- Engine: `iceberg`
- Catalog: `iceberg/warehouse`
- Namespace: `paimon/events/ns1` (Paimon databases use the `namespace` segment)
- Leaf object: `lance/lake/ns1/users`

Roles are managed via `POST /management/v1/security/roles` (insert-only, duplicates ignored) and `DELETE /management/v1/security/roles`. Each write bumps a DB revision counter. Every instance reloads policies on `refresh-interval` when the revision changes: a new Casbin enforcer is built and swapped atomically (no empty-policy window during reload).

## Default roles

| Role                                       | Scope                                                                                   |
|--------------------------------------------|-----------------------------------------------------------------------------------------|
| `IcebergCatalogAdmin`                      | All `Iceberg*` permissions plus `RoleBindingGet`, `RoleBindingAdd`, `RoleBindingDelete` |
| `IcebergCatalogEditor`                     | Iceberg table/namespace/view mutations and reads                                        |
| `IcebergCatalogViewer`                     | Iceberg `*List`, `*Get`, `*Exists`                                                      |
| `PaimonCatalogAdmin` / `Editor` / `Viewer` | Same pattern with `Paimon*` permissions                                                 |
| `LanceCatalogAdmin` / `Editor` / `Viewer`  | Same pattern with `Lance*` permissions                                                  |

Permission names are defined in `authorization-spi` (`Permission` enum), e.g. `IcebergTableList`, `PaimonDatabaseCreate`, `RoleBindingGet`.

## Casbin model

```
r = sub, obj, perm
p = sub, obj, perm
m = r.sub == p.sub && resourcePrefixMatch(r.obj, p.obj) && globMatch(r.perm, p.perm)
```

Inheritance is handled by `resourcePrefixMatch`: a binding at `iceberg/prod` grants access to `iceberg/prod/analytics/orders`.
