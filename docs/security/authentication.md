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
kasanari.authentication.oidc.clock-skew-seconds=30

# Discovery / connection (Quarkus OIDC common defaults when omitted)
kasanari.authentication.oidc.discovery-path=.well-known/openid-configuration
kasanari.authentication.oidc.discovery-enabled=true
kasanari.authentication.oidc.connection-timeout=10s
kasanari.authentication.oidc.connection-retry-count=3
kasanari.authentication.oidc.connection-delay=2s
kasanari.authentication.oidc.follow-redirects=true
kasanari.authentication.oidc.use-blocking-dns-lookup=false
kasanari.authentication.oidc.max-pool-size=20

# Proxy (named Quarkus proxy config and/or legacy host proxy)
kasanari.authentication.oidc.proxy-configuration-name=my-proxy
kasanari.authentication.oidc.proxy-host=proxy.example.com
kasanari.authentication.oidc.proxy-port=8080
kasanari.authentication.oidc.proxy-username=proxy-user
kasanari.authentication.oidc.proxy-password=proxy-secret

# TLS (named Quarkus TLS config and/or store files)
kasanari.authentication.oidc.tls-configuration-name=oidc-tls
kasanari.authentication.oidc.tls-verification=REQUIRED
kasanari.authentication.oidc.trust-store-file=/path/to/truststore.p12
kasanari.authentication.oidc.trust-store-password=changeit
kasanari.authentication.oidc.trust-store-cert-alias=oidc
kasanari.authentication.oidc.trust-store-file-type=PKCS12
kasanari.authentication.oidc.key-store-file=/path/to/keystore.p12
kasanari.authentication.oidc.key-store-password=changeit
kasanari.authentication.oidc.key-store-file-type=PKCS12
kasanari.authentication.oidc.key-store-key-alias=client
kasanari.authentication.oidc.key-store-key-password=changeit
```

If `client-id` is set, Kasanari validates audience (`aud`) / authorized party (`azp`) claims.

`connection-timeout` accepts Quarkus-style values such as `10s` / `500ms` or ISO-8601 (`PT10S`).

When `tls-configuration-name` or `proxy-configuration-name` is set, the matching Quarkus registry bean must be available at startup.

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
- `kasanari.authentication.oidc.connection-timeout` -> `KASANARI_AUTHENTICATION_OIDC_CONNECTION_TIMEOUT`
- `kasanari.authentication.oidc.tls-configuration-name` -> `KASANARI_AUTHENTICATION_OIDC_TLS_CONFIGURATION_NAME`

## Custom authentication provider SPI

To implement a custom provider:

1. Implement `kasanari.authentication.spi.AuthProvider`.
2. Register implementation in `META-INF/services/kasanari.authentication.spi.AuthProvider`.
3. Return your custom `type()`.
4. Configure `kasanari.authentication.type=<your-type>`.

Reference example: `examples/spi/README.md`.
