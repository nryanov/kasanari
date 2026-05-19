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
export KASANARI_AUTH_LDAP_URL=ldap://localhost:389
export KASANARI_AUTH_LDAP_BIND_PRINCIPAL=cn=admin,dc=kasanari,dc=local
export KASANARI_AUTH_LDAP_BIND_PASSWORD=admin
export KASANARI_AUTH_LDAP_SEARCH_BASE_DN=ou=users,dc=kasanari,dc=local
export KASANARI_AUTH_LDAP_RDN_IDENTIFIER=uid

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
