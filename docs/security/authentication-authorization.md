# Authentication and Authorization

Kasanari separates identity (authentication) from permission checks (authorization).

## Authentication

Configuration key:

- `kasanari.authentication.type`

Built-in provider types:

- `none` (default)
- `ldap`
- `oidc`

SPI entrypoint:

- `kasanari.authentication.spi.AuthProvider`

Provider contract:

- `type()` identifies provider name.
- `initialize(AuthProviderContext)` receives configuration context.
- `authenticate(AuthRequest)` returns authenticated principal or empty.

### Runtime behavior

- HTTP auth is handled by `KasanariHttpAuthenticationMechanism`.
- Public endpoints are controlled by provider metadata.
- Typical public paths include `/q/health`, `/q/openapi`, and `/docs`.

## Authorization

Configuration key:

- `kasanari.authorization.type`

Built-in provider types:

- `allow-all` (default)
- `casbin`

SPI entrypoint:

- `kasanari.authorization.spi.AuthorizationProvider`

Permissions are represented by `Permission` enum in `authorization-spi`.

### Management permissions

Catalog metadata operations are mapped by catalog type:

- `IcebergCatalogCreate`, `IcebergCatalogGet`, `IcebergCatalogUpdate`, `IcebergCatalogDelete`
- `PaimonCatalogCreate`, `PaimonCatalogGet`, `PaimonCatalogUpdate`, `PaimonCatalogDelete`
- `LanceCatalogCreate`, `LanceCatalogGet`, `LanceCatalogUpdate`, `LanceCatalogDelete`

Role administration permissions:

- `RoleSelect`
- `RoleAdd`
- `RoleRemove`

## Casbin role model

When `kasanari.authorization.type=casbin`, default role families are available per catalog type:

- `IcebergCatalogAdmin`, `IcebergCatalogEditor`, `IcebergCatalogViewer`
- `PaimonCatalogAdmin`, `PaimonCatalogEditor`, `PaimonCatalogViewer`
- `LanceCatalogAdmin`, `LanceCatalogEditor`, `LanceCatalogViewer`

Role bindings are managed through Management API:

- `GET /management/v1/security/roles`
- `PUT /management/v1/security/roles`
- `DELETE /management/v1/security/roles`

## Configuration examples

### None auth + allow-all authz (local default)

```properties
kasanari.authentication.type=none
kasanari.authorization.type=allow-all
```

### LDAP auth + Casbin authz

```properties
kasanari.authentication.type=ldap
kasanari.authentication.ldap.url=ldap://localhost:389
kasanari.authentication.ldap.bind-principal=cn=admin,dc=kasanari,dc=local
kasanari.authentication.ldap.bind-password=admin
kasanari.authentication.ldap.search-base-dn=ou=users,dc=kasanari,dc=local
kasanari.authentication.ldap.rdn-identifier=uid

kasanari.authorization.type=casbin
kasanari.authorization.casbin.superuser-subjects=root,admin
```

### OIDC auth + Casbin authz

```properties
kasanari.authentication.type=oidc
kasanari.authentication.oidc.issuer-url=http://localhost:8080/realms/kasanari
kasanari.authentication.oidc.client-id=kasanari-api
kasanari.authentication.oidc.client-secret=change-me

kasanari.authorization.type=casbin
```
