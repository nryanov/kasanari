# Iceberg + Trino (internal)

This scaffold uses Trino as the query engine and Kasanari with Iceberg `INTERNAL` mode.

## Prerequisites

- Docker and Docker Compose plugin
- `curl` and `jq`

Build the local Kasanari image from repository root:

```shell
./scripts/build-container-images.sh
```

## Startup

```shell
cd examples/iceberg/trino/internal
docker compose up -d
```

## Register catalog via API

```shell
curl -sS -X POST "http://localhost:9090/management/v1/catalogs" \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": "iceberg_trino_internal",
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

## Sample operations and expected outcomes

- Run SQL smoke check:

```shell
docker compose exec trino trino --execute "$(cat queries/example.sql)"
```

- Expected output contains `trino_internal_ready = 1`.
- Verify catalog exists:

```shell
curl -sS "http://localhost:9090/management/v1/catalogs/ICEBERG/iceberg_trino_internal" | jq .
```

## Teardown

```shell
docker compose down -v
```
