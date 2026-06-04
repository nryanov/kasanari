# Iceberg + Trino (proxy JDBC)

This scaffold demonstrates Kasanari Iceberg `PROXY` mode with JDBC-oriented proxy configuration.

## Prerequisites

- Docker and Docker Compose plugin
- `curl` and `jq`

Build the local Kasanari image from repository root:

```shell
./scripts/build-container-images.sh
```

## Startup

```shell
cd examples/iceberg/trino/proxy-jdbc
docker compose up -d
```

## Register catalog via API

```shell
curl -sS -X POST "http://localhost:9095/management/v1/catalogs" \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": "iceberg_trino_proxy_jdbc",
    "catalogType": "ICEBERG",
    "mode": "PROXY",
    "spec": {
      "fileIoProperties": {
        "fs.s3a.endpoint": "http://minio:9000",
        "fs.s3a.access.key": "admin",
        "fs.s3a.secret.key": "password",
        "fs.s3a.path.style.access": "true",
        "fs.s3a.connection.ssl.enabled": "false"
      },
      "catalogProperties": {
        "catalog-impl": "org.apache.iceberg.jdbc.JdbcCatalog",
        "uri": "jdbc:postgresql://catalog-storage:5432/postgres",
        "jdbc.user": "postgres",
        "jdbc.password": "postgres",
        "warehouse": "s3a://warehouse",
        "io-impl": "org.apache.iceberg.aws.s3.S3FileIO",
        "s3.endpoint": "http://minio:9000",
        "s3.access-key-id": "admin",
        "s3.secret-access-key": "password",
        "s3.path-style-access": "true",
        "s3.client-factory": "kasanari.catalog.iceberg.s3.NoneRegionS3FileIOAwsClientFactory"
      }
    }
  }'
```

## Sample operations and expected outcomes

- Run SQL smoke check:

```shell
docker compose exec trino trino --execute "$(cat queries/01_smoke.sql)"
```

- Expected output contains `trino_proxy_jdbc_ready = 1`.
- Expected registration response: HTTP `201` and subsequent GET on catalog metadata returns `200`.

## Teardown

```shell
docker compose down -v
```
