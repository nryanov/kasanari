# Paimon Spark proxy-jdbc example

Runs Kasanari with a PAIMON catalog in `PROXY` mode and a Spark-enabled Jupyter client.

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
cd examples/paimon/spark/proxy-jdbc
docker compose up -d
```

Open Jupyter at `http://localhost:8888` with token `kasanari`, then run `paimon-spark-proxy-jdbc.ipynb`.

## Register catalog

Primary (JDBC-oriented proxy) payload:

```shell
curl -s -X POST http://localhost:9090/management/v1/catalogs \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": "paimon_spark_proxy_jdbc",
    "catalogType": "PAIMON",
    "mode": "PROXY",
    "spec": {
      "fileIoProperties": {
        "fs.s3a.access.key": "admin",
        "fs.s3a.secret.key": "password",
        "fs.s3a.impl": "org.apache.hadoop.fs.s3a.S3AFileSystem",
        "fs.s3a.path.style.access": "true",
        "fs.s3a.endpoint": "http://minio:9000"
      },
      "catalogProperties": {
        "type": "jdbc",
        "warehouse": "s3a://warehouse",
        "jdbc-url": "jdbc:postgresql://catalog-storage:5432/postgres",
        "jdbc-user": "postgres",
        "jdbc-password": "postgres",
        "jdbc-driver": "org.postgresql.Driver",
        "jdbc-table-prefix": "paimon_"
      }
    }
  }'
```

Best-effort constraint: this repository already supports filesystem proxy reliably; if your JDBC proxy connector combo fails, fallback to:

```json
{"type":"filesystem","warehouse":"s3a://warehouse"}
```

inside `spec.catalogProperties`.

## Sample operations and expected outcomes

- `GET /management/v1/catalogs/PAIMON/paimon_spark_proxy_jdbc` returns `200` and mode `PROXY`.
- `GET /management/v1/catalogs` includes `paimon_spark_proxy_jdbc`.
- `GET /q/health` returns `200`.
- The notebook includes Spark SQL catalog lifecycle operations: create database/table, insert/select, alter table, create/query view, delete rows, drop view/table.

The notebook runs these checks and prints the status/results.

## Teardown

```shell
docker compose down -v
```
