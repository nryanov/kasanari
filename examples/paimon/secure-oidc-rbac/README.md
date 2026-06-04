# Paimon secure OIDC + RBAC example

Runs Kasanari with OIDC authentication (`keycloak`) and Casbin RBAC authorization for PAIMON catalogs.

## Prerequisites

- Docker and Docker Compose.
- Built Kasanari image:

```shell
./scripts/build-container-images.sh
```

## Startup

From repository root:

```shell
cd examples/paimon/secure-oidc-rbac
docker compose up -d
```

Wait for Keycloak readiness:

```shell
curl -s http://localhost:8080/realms/kasanari/.well-known/openid-configuration
```

## Register catalog command example

Get a demo-user token:

```shell
TOKEN=$(curl -s -X POST "http://localhost:8080/realms/kasanari/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=kasanari-api" \
  -d "client_secret=kasanari-api-secret" \
  -d "username=demo" \
  -d "password=demo" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')
```

Register PAIMON catalog:

```shell
curl -s -X POST http://localhost:9090/management/v1/catalogs \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": "paimon_secure_oidc",
    "catalogType": "PAIMON",
    "mode": "INTERNAL",
    "spec": {
      "fileIoProperties": {
        "fs.s3a.access.key": "admin",
        "fs.s3a.secret.key": "password",
        "fs.s3a.impl": "org.apache.hadoop.fs.s3a.S3AFileSystem",
        "fs.s3a.path.style.access": "true",
        "fs.s3a.endpoint": "http://minio:9000"
      },
      "catalogProperties": {
        "warehouse": "s3a://warehouse",
        "uri": "jdbc:postgresql://catalog-storage:5432/postgres",
        "kasanari.jdbc.user": "postgres",
        "kasanari.jdbc.password": "postgres",
        "kasanari.catalog.key": "paimon_secure_oidc"
      }
    }
  }'
```

## RBAC bootstrap (`/management/v1/security/roles`)

Bind demo user to viewer role:

```shell
curl -s -X PUT http://localhost:9090/management/v1/security/roles \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "bindings": [
      {
        "subject": "demo",
        "catalogType": "PAIMON",
        "role": "PaimonCatalogViewer"
      }
    ]
  }'
```

Verify role binding:

```shell
curl -s -H "Authorization: Bearer ${TOKEN}" \
  "http://localhost:9090/management/v1/security/roles?subject=demo&catalogType=PAIMON"
```

## Sample operations and expected outcomes

- Without token: `GET /management/v1/catalogs` returns `401`.
- With `TOKEN`: `GET /management/v1/catalogs/PAIMON/paimon_secure_oidc` returns `200`.
- Run SQL smoke query from the client container:

```shell
docker compose exec client psql "postgresql://postgres:postgres@catalog-storage:5432/postgres" \
  -f queries/01_smoke.sql
```

## Teardown

```shell
docker compose down -v
```
