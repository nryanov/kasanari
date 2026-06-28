# Authorization Providers

Authorization is selected with:

```properties
kasanari.authorization.type=<provider>
```

Built-in values:

- `allow-all`
- `casbin`

Custom provider types are supported through SPI.

## Provider: allow-all

`allow-all` permits every authorized request.

```properties
kasanari.authorization.type=allow-all
```

Use for local development or explicitly trusted environments.

## Provider: casbin

`casbin` enables RBAC with role bindings persisted in PostgreSQL.

### Minimal config

```properties
kasanari.authorization.type=casbin
```

JDBC settings are resolved from:

1. `kasanari.authorization.casbin.*` (if provided), otherwise
2. `kasanari.management.metadata.jdbc-properties.*`

### Optional superusers

Superusers bypass permission checks:

```properties
kasanari.authorization.casbin.superuser-subjects=root,admin
```

If unset, default superuser subject is `root`.

### Role binding management API

Casbin role assignments are managed through:

- `GET /management/v1/security/roles`
- `POST /management/v1/security/roles`
- `DELETE /management/v1/security/roles`

Binding model:

```json
{
  "subject": "alice",
  "role": "IcebergCatalogViewer",
  "resource": "ICEBERG/warehouse/analytics"
}
```

`resource` is a fully qualified scope path (no wildcards). The catalog engine is the first segment (`ICEBERG`, `PAIMON`, or `LANCE`). Hierarchy is expressed by path depth; Casbin applies prefix inheritance at enforcement time.

## HTTP outcomes

- `401 Unauthorized`: authentication failed or missing.
- `403 Forbidden`: authenticated principal lacks permission.

## Custom authorization provider SPI

To implement a custom provider:

1. Implement `kasanari.authorization.spi.AuthorizationProvider`.
2. Register implementation in `META-INF/services/kasanari.authorization.spi.AuthorizationProvider`.
3. Return your custom `type()`.
4. Configure `kasanari.authorization.type=<your-type>`.

Reference example: `examples/authorization/custom/README.md`.
