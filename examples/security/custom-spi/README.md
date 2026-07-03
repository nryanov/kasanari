# Full example: custom authentication + authorization SPI with Docker

This example runs Kasanari with:

- custom authentication provider from `examples/authentication/custom`
- custom authorization provider from `examples/authorization/custom`
- PostgreSQL metadata database in Docker Compose

Both custom providers are loaded at runtime via `SPI_EXT_DIR`.

## Prerequisites

- Docker + Docker Compose
- Java 21+
- Build tools used by this repository (`./gradlew`)

## 1) Build custom SPI jars

From the repository root:

```shell
./gradlew :examples:authentication:custom:jar :examples:authorization:custom:jar
```

Expected artifacts:

- `examples/authentication/custom/build/libs/auth-custom-example.jar`
- `examples/authorization/custom/build/libs/authorization-custom-example.jar`

## 2) Build Kasanari Docker image

From the repository root:

```shell
./scripts/build-container-images.sh
```

The default image tag is `local/kasanari:0.1.0`.

## 3) Start the stack

From this directory:

```shell
cd examples/security/custom-spi
cp .env.example .env
docker compose up -d
```

This compose stack starts:

- `catalog-storage` (PostgreSQL)
- `kasanari` (with both custom SPI jars mounted into `/opt/kasanari/spi`)

## 4) Verify behavior

Wait until server startup is complete:

```shell
docker compose logs kasanari | rg "started in"
```

Metrics endpoint without auth header is rejected by custom authentication provider (expect `401`):

```shell
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:9090/q/metrics
```

Metrics endpoint with a wrong token is rejected (expect `401`):

```shell
curl -s -o /dev/null -w "%{http_code}\n" \
  -H "X-Kasanari-Token: wrong-token" \
  http://localhost:9090/q/metrics
```

Metrics endpoint with the configured token is accepted (expect `200`):

```shell
curl -s -o /dev/null -w "%{http_code}\n" \
  -H "X-Kasanari-Token: dev-secret" \
  http://localhost:9090/q/metrics
```

## 5) Optional: force authorization deny

Restart Kasanari with a disallowed subject list:

```shell
KASANARI_ALLOWED_SUBJECTS=another-user docker compose up -d --force-recreate kasanari
```

Now call management security endpoint with a valid token (expect `403`):

```shell
curl -i \
  -H "X-Kasanari-Token: dev-secret" \
  "http://localhost:9090/management/v1/security/roles?subject=iceberg/default"
```

`subject` is used here because current generated API mapping passes this query value as the handler resource argument.

Revert to default allow-list:

```shell
docker compose up -d --force-recreate kasanari
```

With default `KASANARI_ALLOWED_SUBJECTS=header-token-user`, the same request passes authorization and reaches role-binding administration (this provider does not implement role-binding administration, so it returns `500` with an explanatory message).

## 6) Stop the stack

```shell
docker compose down
```
