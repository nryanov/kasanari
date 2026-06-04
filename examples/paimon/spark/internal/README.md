# Paimon Spark internal example

Runs Kasanari with a PAIMON catalog in `INTERNAL` mode and a Spark-enabled Jupyter client.

## Prerequisites

- Docker and Docker Compose.
- Built Kasanari image:

```shell
./scripts/build-container-images.sh
```

- Built Spark Jupyter image:

```shell
docker build -f examples/common/jupyter/Dockerfile -t local/jupyter-spark:0.1.0 examples/common/jupyter
```

## Startup

From repository root:

```shell
cd examples/paimon/spark/internal
docker compose up -d
```

Open Jupyter at `http://localhost:8888` with token `kasanari`, then run `paimon-spark-internal.ipynb`.

## Register catalog

The notebook includes this registration call:

```shell
curl -s -X POST http://localhost:9090/management/v1/catalogs \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": "paimon_spark_internal",
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
        "kasanari.catalog.key": "paimon_spark_internal"
      }
    }
  }'
```

## Sample operations and expected outcomes

- `GET /management/v1/catalogs/PAIMON/paimon_spark_internal` returns `200` with the registered spec.
- `GET /management/v1/catalogs` includes `paimon_spark_internal`.
- `GET /q/health` returns `200`.
- The notebook includes Spark SQL catalog lifecycle operations: create database/table, insert/select, alter table, create/query view, delete rows, drop view/table.

The notebook executes these calls and prints status codes and payload snippets.

## Teardown

```shell
docker compose down -v
```
