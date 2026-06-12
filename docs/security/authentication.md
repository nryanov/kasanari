# Authentication Providers

Authentication is selected with:

```properties
kasanari.authentication.type=<provider>
```

Built-in values:

- `none`
- `ldap`
- `oidc`

Custom provider types are also supported through SPI.

## Provider: none

`none` disables authentication checks and allows all requests to reach authorization.

```properties
kasanari.authentication.type=none
```

Use this for local development or trusted environments only.

## Provider: ldap

`ldap` authenticates HTTP Basic credentials against LDAP.

### Required properties

```properties
kasanari.authentication.type=ldap
kasanari.authentication.ldap.url=ldap://localhost:389
kasanari.authentication.ldap.bind-principal=cn=admin,dc=kasanari,dc=local
kasanari.authentication.ldap.bind-password=admin
kasanari.authentication.ldap.search-base-dn=ou=users,dc=kasanari,dc=local
```

### Optional properties

```properties
kasanari.authentication.ldap.rdn-identifier=uid
```

### Important runtime note

LDAP provider relies on JNDI and requires:

```properties
quarkus.naming.enable-jndi=true
```

This is a build-time Quarkus setting; restart the application after changing it.

### Verification

Without credentials:

```bash
curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:9090/management/v1/catalogs"
```

With Basic credentials:

```bash
curl -s -o /dev/null -w "%{http_code}\n" \
  -u alice:alice \
  "http://localhost:9090/management/v1/catalogs"
```

Runnable environment: `examples/authentication/ldap/README.md`.

## Provider: oidc

`oidc` validates Bearer JWT tokens using OIDC discovery metadata (`issuer` + `jwks_uri`).

### Required properties

```properties
kasanari.authentication.type=oidc
kasanari.authentication.oidc.issuer-url=http://localhost:8080/realms/kasanari
```

### Optional properties

```properties
kasanari.authentication.oidc.client-id=kasanari-api
```

If `client-id` is set, Kasanari validates audience (`aud`) / authorized party (`azp`) claims.

### Verification

Without token:

```bash
curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:9090/management/v1/catalogs"
```

With token:

```bash
curl -s -o /dev/null -w "%{http_code}\n" \
  -H "Authorization: Bearer ${TOKEN}" \
  "http://localhost:9090/management/v1/catalogs"
```

Runnable environment: `examples/authentication/oidc/README.md`.

## Environment variable mapping

Quarkus maps dot-separated properties to uppercase underscore names:

- `kasanari.authentication.type` -> `KASANARI_AUTHENTICATION_TYPE`
- `kasanari.authentication.ldap.url` -> `KASANARI_AUTHENTICATION_LDAP_URL`
- `kasanari.authentication.oidc.issuer-url` -> `KASANARI_AUTHENTICATION_OIDC_ISSUER_URL`

## Custom authentication provider SPI

To implement a custom provider:

1. Implement `kasanari.authentication.spi.AuthProvider`.
2. Register implementation in `META-INF/services/kasanari.authentication.spi.AuthProvider`.
3. Return your custom `type()`.
4. Configure `kasanari.authentication.type=<your-type>`.

Reference example: `examples/authentication/custom/README.md`.
