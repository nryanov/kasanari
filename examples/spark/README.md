# Spark notebooks (Iceberg, Paimon, Lance)

This example runs Kasanari with Spark/Jupyter and provides notebooks for:

- Iceberg `INTERNAL`
- Iceberg `PROXY` (`JdbcCatalog`)
- Paimon `INTERNAL`
- Lance `INTERNAL`

## Prerequisites

- Docker and Docker Compose plugin
- `curl` and `jq`

Build the local Kasanari image from repository root:

```shell
./scripts/build-container-images.sh
```

## Startup

```shell
cd examples/spark
docker compose up -d
```

Open Jupyter at `http://localhost:8889` with token `iceberg`.

## Register catalog via API

```shell
curl -sS -X POST "http://localhost:9090/management/v1/catalogs" \
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

- Run notebook(s) in `notebooks/`:
  - `iceberg-kasanari.ipynb`
  - `iceberg-jdbc-proxy.ipynb`
  - `paimon-kasanari.ipynb`
  - `lance-kasanari.ipynb`
- Expected catalog registration response: HTTP `201`.

## Teardown

```shell
docker compose down -v
```
