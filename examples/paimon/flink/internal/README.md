# Paimon Flink internal example

Runs Kasanari with a PAIMON catalog in `INTERNAL` mode plus a Flink SQL client container and a Jupyter notebook.

## Prerequisites

- Docker and Docker Compose.
- Built Kasanari image:

```shell
./scripts/build-container-images.sh
```

## Startup

From repository root:

```shell
cd examples/paimon/flink/internal
docker compose up -d
```

Open Jupyter at `http://localhost:8888` with token `kasanari`, then run `paimon-flink-internal.ipynb`.

## Register catalog

Use the notebook or run:

```shell
curl -s -X POST http://localhost:9090/management/v1/catalogs \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": "paimon_flink_internal",
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
        "kasanari.catalog.key": "paimon_flink_internal"
      }
    }
  }'
```

## Sample operations and expected outcomes

- `GET /management/v1/catalogs/PAIMON/paimon_flink_internal` returns `200`.
- `GET /management/v1/catalogs` shows this catalog in the PAIMON list.
- `GET /q/health` returns `200`.

The notebook performs these checks; the `flink-sql-client` container is included for follow-up Flink-side experiments.

## Teardown

```shell
docker compose down -v
```
