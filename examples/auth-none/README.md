# Auth example: none (default)

No authentication is required. All API routes are permitted.

## Start backing services

```shell
docker compose up -d
```

## Run Kasanari

From the repository root, with env vars from this directory:

```shell
set -a && source examples/auth-none/kasanari.env && set +a
./gradlew :modules:server:quarkusDev
```

Or set `kasanari.auth.type=none` in `modules/server/src/main/resources/application.properties` (default).

## Verify

```shell
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:9090/q/health
```

Expect `200` without credentials.

## Switch auth mode

Change `KASANARI_AUTH_TYPE` or `kasanari.auth.type` to `none`, `ldap`, `oauth`, or a custom SPI type. Restart the application. No rebuild is required unless you add a new provider jar.
