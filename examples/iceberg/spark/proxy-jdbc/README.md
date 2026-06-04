# Iceberg + Spark (proxy JDBC)

This example runs Kasanari with an Iceberg `PROXY` catalog configured for `JdbcCatalog`-style properties.

## Prerequisites

- Docker and Docker Compose plugin
- `curl` and `jq`

Build the local Kasanari image from repository root:

```shell
./scripts/build-container-images.sh
```

Build the Spark Jupyter image used by this example:

```shell
docker build -f examples/common/jupyter/Dockerfile -t local/jupyter-spark:0.1.0 examples/common/jupyter
```

## Startup

```shell
cd examples/iceberg/spark/proxy-jdbc
docker compose up -d
```

Open Jupyter at `http://localhost:8889` with token `iceberg`.

## Register catalog via API

```shell
curl -sS -X POST "http://localhost:9091/management/v1/catalogs" \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": "iceberg_spark_proxy_jdbc",
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

- Run `notebooks/01_register_and_list.ipynb` inside Jupyter.
- The notebook now includes Spark SQL catalog lifecycle operations: create namespace/table, insert/select, alter table, create/query view, delete rows, drop view/table.
- Expected catalog registration response: HTTP `201`.
- Expected metadata read response: HTTP `200` with `catalogId` equal to `iceberg_spark_proxy_jdbc`.

## Teardown

```shell
docker compose down -v
```
