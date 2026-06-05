# Lance secure OIDC + RBAC example

Runs Kasanari with OIDC authentication (`keycloak`) and Casbin RBAC authorization for LANCE catalogs.

## Prerequisites

- Docker and Docker Compose.
- Built Kasanari image:

```shell
./scripts/build-container-images.sh
```

## Startup

From repository root:

```shell
cd examples/lance/secure-oidc-rbac
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

Register LANCE catalog:

```shell
curl -s -X POST http://localhost:9090/management/v1/catalogs \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": "lance_secure_oidc",
    "catalogType": "LANCE",
    "mode": "PROXY",
    "spec": {
      "fileIoProperties": {},
      "catalogProperties": {
        "implementation": "dir",
        "root": "s3://warehouse",
        "manifest_enabled": "false",
        "dir_listing_enabled": "true",
        "storage.aws_access_key_id": "admin",
        "storage.aws_secret_access_key": "password",
        "storage.aws_endpoint": "http://minio:9000",
        "storage.aws_allow_http": "true",
        "storage.aws_virtual_hosted_style_request": "false",
        "storage.access_key_id": "admin",
        "storage.secret_access_key": "password",
        "storage.endpoint": "http://minio:9000",
        "storage.allow_http": "true",
        "storage.virtual_hosted_style_request": "false",
        "storage.region": "us-east-1"
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
        "catalogType": "LANCE",
        "role": "LanceCatalogViewer"
      }
    ]
  }'
```

Verify role binding:

```shell
curl -s -H "Authorization: Bearer ${TOKEN}" \
  "http://localhost:9090/management/v1/security/roles?subject=demo&catalogType=LANCE"
```

## Sample operations and expected outcomes

- Without token: `GET /management/v1/catalogs` returns `401`.
- With `TOKEN`: `GET /management/v1/catalogs/LANCE/lance_secure_oidc` returns `200`.
- Run SQL smoke query from the client container:

```shell
docker compose exec client psql "postgresql://postgres:postgres@catalog-storage:5432/postgres" \
  -f queries/example.sql
```

## Teardown

```shell
docker compose down -v
```
