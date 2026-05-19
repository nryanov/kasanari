# Auth example: LDAP (HTTP Basic)

LDAP authentication with HTTP Basic. Test users: `alice` / `alice`, `bob` / `bob`.

## Start backing services

```shell
cd examples/auth-ldap
docker compose up -d
```

Wait until OpenLDAP is ready (a few seconds after container start).

## Run Kasanari

Set auth mode and LDAP settings (see `application.properties.sample`), then start the server:

```shell
export KASANARI_AUTH_TYPE=ldap
export QUARKUS_SECURITY_LDAP_DIR_CONTEXT_URL=ldap://localhost:389
export QUARKUS_SECURITY_LDAP_DIR_CONTEXT_PRINCIPAL=cn=admin,dc=kasanari,dc=local
export QUARKUS_SECURITY_LDAP_DIR_CONTEXT_PASSWORD=admin
export QUARKUS_SECURITY_LDAP_IDENTITY_MAPPING_SEARCH_BASE_DN=ou=users,dc=kasanari,dc=local
export QUARKUS_SECURITY_LDAP_IDENTITY_MAPPING_RDN_IDENTIFIER=uid

./gradlew :modules:server:quarkusDev
```

## Verify

Without credentials (expect 401):

```shell
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:9090/management/v1/catalogs
```

With Basic auth (expect 200 or 404 depending on data):

```shell
curl -s -o /dev/null -w "%{http_code}\n" -u alice:alice http://localhost:9090/management/v1/catalogs
```

Health and Swagger UI stay public: `/q/health`, `/docs`.

## Switch auth mode

Change `KASANARI_AUTH_TYPE` or `kasanari.auth.type` to `none` or `oauth`, restart the application. No rebuild is required.
