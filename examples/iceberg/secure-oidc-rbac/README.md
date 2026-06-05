# Iceberg secure OIDC + RBAC

This example enables OIDC authentication and Casbin RBAC authorization for Kasanari, then demonstrates role bootstrap with `/management/v1/security/roles`.

## Prerequisites

- Docker and Docker Compose plugin
- `curl` and `jq`

Build the local Kasanari image from repository root:

```shell
./scripts/build-container-images.sh
```

## Startup

```shell
cd examples/iceberg/secure-oidc-rbac
docker compose up -d
```

Wait for Keycloak discovery endpoint:

```shell
curl -sS http://localhost:8084/realms/kasanari/.well-known/openid-configuration | jq .issuer
```

## Obtain token

```shell
TOKEN=$(curl -sS -X POST "http://localhost:8084/realms/kasanari/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=kasanari-api" \
  -d "client_secret=kasanari-api-secret" \
  -d "username=demo" \
  -d "password=demo" | jq -r .access_token)
```

## Register catalog via API

```shell
curl -sS -X POST "http://localhost:9102/management/v1/catalogs" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": "iceberg_secure_internal",
    "catalogType": "ICEBERG",
    "mode": "INTERNAL",
    "spec": {
      "fileIoProperties": {},
      "catalogProperties": {
        "uri": "jdbc:postgresql://catalog-storage:5432/postgres",
        "warehouse": "s3a://warehouse",
        "io-impl": "org.apache.iceberg.aws.s3.S3FileIO",
        "s3.endpoint": "http://minio:9000",
        "s3.access-key-id": "admin",
        "s3.secret-access-key": "password",
        "s3.path-style-access": "true",
        "s3.client-factory": "kasanari.catalog.iceberg.s3.NoneRegionS3FileIOAwsClientFactory",
        "kasanari.jdbc.user": "postgres",
        "kasanari.jdbc.password": "postgres"
      }
    }
  }'
```

## Bootstrap RBAC roles

Create role bindings:

```shell
curl -sS -X PUT "http://localhost:9102/management/v1/security/roles" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "bindings": [
      {
        "subject": "demo",
        "catalogType": "ICEBERG",
        "role": "IcebergCatalogViewer"
      }
    ]
  }'
```

List role bindings:

```shell
curl -sS "http://localhost:9102/management/v1/security/roles" \
  -H "Authorization: Bearer ${TOKEN}" \
  --get --data-urlencode "subject=demo" --data-urlencode "catalogType=ICEBERG" | jq .
```

## Sample operations and expected outcomes

- Registration should return HTTP `201`.
- Role binding creation should return HTTP `200`.
- Run Trino client smoke SQL:

```shell
docker compose exec trino trino --execute "$(cat queries/example.sql)"
```

- Expected SQL output includes `secure_oidc_rbac_ready = 1`.

## Teardown

```shell
docker compose down -v
```
