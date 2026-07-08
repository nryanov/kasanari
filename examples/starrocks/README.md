# Iceberg + StarRocks (internal)

This scaffold runs Kasanari with an Iceberg `INTERNAL` catalog and a StarRocks all-in-one engine service.

## Prerequisites

- Docker and Docker Compose plugin
- `curl` and `jq`

## Startup

```shell
cd examples/starrocks
docker compose up -d
```

## Register catalog via API

```shell
curl -sS -X POST "http://localhost:9090/management/v1/catalogs" \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": "iceberg_starrocks_internal",
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

- Run SQL smoke query:

```shell
docker compose exec starrocks mysql -h 127.0.0.1 -P 9030 -uroot -e "$(cat queries/example.sql)"
```

- Expected output contains `starrocks_internal_ready` and value `1`.
- Catalog metadata endpoint should return HTTP `200`.

## Teardown

```shell
docker compose down -v
```
