# Iceberg + StarRocks (proxy JDBC)

This scaffold configures Kasanari Iceberg `PROXY` mode with JDBC-oriented proxy properties.

## Prerequisites

- Docker and Docker Compose plugin
- `curl` and `jq`

Build the local Kasanari image from repository root:

```shell
./scripts/build-container-images.sh
```

## Startup

```shell
cd examples/iceberg/starrocks/proxy-jdbc
docker compose up -d
```

## Register catalog via API

```shell
curl -sS -X POST "http://localhost:9099/management/v1/catalogs" \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": "iceberg_starrocks_proxy_jdbc",
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

- Run SQL smoke query:

```shell
docker compose exec starrocks mysql -h 127.0.0.1 -P 9030 -uroot -e "$(cat queries/01_smoke.sql)"
```

- Expected output contains `starrocks_proxy_jdbc_ready = 1`.
- Best-effort note: StarRocks Iceberg federation behavior depends on version and connectors; if direct Iceberg validation is constrained, use Trino proxy-jdbc example as fallback verification.

## Teardown

```shell
docker compose down -v
```
