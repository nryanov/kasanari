# Auth example: OAuth / OIDC (Bearer)

Bearer token authentication via Keycloak. Realm `kasanari`, client `kasanari-api`, demo user `demo` / `demo`.

## Start backing services

```shell
cd examples/auth-oauth
docker compose up -d
```

Wait until Keycloak is ready (check `http://localhost:8080/realms/kasanari/.well-known/openid-configuration`).

## Run Kasanari

```shell
export KASANARI_AUTH_TYPE=oauth
export QUARKUS_OIDC_AUTH_SERVER_URL=http://localhost:8080/realms/kasanari
export QUARKUS_OIDC_CLIENT_ID=kasanari-api
export QUARKUS_OIDC_CREDENTIALS_SECRET=kasanari-api-secret

./gradlew :modules:server:quarkusDev
```

## Obtain a token

Password grant (demo only):

```shell
TOKEN=$(curl -s -X POST "http://localhost:8080/realms/kasanari/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=kasanari-api" \
  -d "client_secret=kasanari-api-secret" \
  -d "username=demo" \
  -d "password=demo" | jq -r .access_token)
```

Client credentials (service account):

```shell
TOKEN=$(curl -s -X POST "http://localhost:8080/realms/kasanari/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=kasanari-api" \
  -d "client_secret=kasanari-api-secret" | jq -r .access_token)
```

## Verify

Without token (expect 401):

```shell
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:9090/management/v1/catalogs
```

With Bearer token:

```shell
curl -s -o /dev/null -w "%{http_code}\n" \
  -H "Authorization: Bearer ${TOKEN}" \
  http://localhost:9090/management/v1/catalogs
```

Health and Swagger UI stay public: `/q/health`, `/docs`.

## Switch auth mode

Change `KASANARI_AUTH_TYPE` or `kasanari.auth.type` to `none` or `ldap`, restart the application. No rebuild is required.
