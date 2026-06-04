# Iceberg + Spark (internal)

This example runs Kasanari with an Iceberg `INTERNAL` catalog backed by Postgres metadata and MinIO object storage.

## Prerequisites

- Docker and Docker Compose plugin
- `curl` and `jq`

Build the local Kasanari image from repository root:

```shell
./scripts/build-container-images.sh
```

## Startup

```shell
cd examples/iceberg/spark/internal
docker compose up -d
```

Open Jupyter at `http://localhost:8888` with token `iceberg`.

## Register catalog via API

```shell
curl -sS -X POST "http://localhost:9090/management/v1/catalogs" \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": "iceberg_spark_internal",
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

- Run `notebooks/01_register_and_list.ipynb` inside Jupyter.
- The notebook now includes Spark SQL catalog lifecycle operations: create namespace/table, insert/select, alter table, create/query view, delete rows, drop view/table.
- Expected catalog registration response: HTTP `201`.
- Expected metadata read response: HTTP `200` with `catalogId` equal to `iceberg_spark_internal`.

## Teardown

```shell
docker compose down -v
```
