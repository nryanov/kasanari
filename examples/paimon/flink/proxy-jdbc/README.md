# Paimon Flink proxy-jdbc example

Runs Kasanari with PAIMON `PROXY` mode and includes a Flink SQL client plus a Jupyter notebook.

## Prerequisites

- Docker and Docker Compose.
- Built Kasanari image:

```shell
./scripts/build-container-images.sh
```

## Startup

From repository root:

```shell
cd examples/paimon/flink/proxy-jdbc
docker compose up -d
```

Open Jupyter at `http://localhost:8888` with token `kasanari`, then run `paimon-flink-proxy-jdbc.ipynb`.

## Register catalog

JDBC-oriented proxy payload:

```shell
curl -s -X POST http://localhost:9090/management/v1/catalogs \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": "paimon_flink_proxy_jdbc",
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

Best-effort note: PAIMON PROXY in this repository is known to work with filesystem proxy (`type=filesystem`) and JDBC proxy depends on your runtime connector set.

## Sample operations and expected outcomes

- `GET /management/v1/catalogs/PAIMON/paimon_flink_proxy_jdbc` returns `200`.
- `GET /management/v1/catalogs` includes the proxy catalog.
- `GET /q/health` returns `200`.

The notebook executes these checks directly.

## Teardown

```shell
docker compose down -v
```
