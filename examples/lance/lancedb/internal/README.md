# LanceDB internal example

Runs Kasanari with a LANCE catalog in `INTERNAL` mode and a Python `lancedb` client container.

## Prerequisites

- Docker and Docker Compose.
- Built Kasanari image:

```shell
./scripts/build-container-images.sh
```

## Startup

From repository root:

```shell
cd examples/lance/lancedb/internal
docker compose up -d
```

## Register catalog command example

```shell
curl -s -X POST http://localhost:9090/management/v1/catalogs \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": "lance_internal",
    "catalogType": "LANCE",
    "mode": "INTERNAL",
    "spec": {
      "fileIoProperties": {},
      "catalogProperties": {
        "implementation": "dir",
        "root": "s3://warehouse",
        "lance.warehouse.location": "s3://warehouse",
        "kasanari.jdbc.user": "postgres",
        "kasanari.jdbc.password": "postgres",
        "uri": "jdbc:postgresql://catalog-storage:5432/postgres",
        "lance.storage.storage_options.aws_access_key_id": "admin",
        "lance.storage.storage_options.aws_secret_access_key": "password",
        "lance.storage.storage_options.aws_endpoint": "http://minio:9000",
        "lance.storage.storage_options.aws_allow_http": "true",
        "lance.storage.storage_options.aws_virtual_hosted_style_request": "false",
        "lance.storage.storage_options.region": "us-east-1"
      }
    }
  }'
```

## Sample operations and expected outcomes

- `GET /management/v1/catalogs/LANCE/lance_internal` returns `200`.
- `GET /management/v1/catalogs` includes `lance_internal`.
- Run SQL smoke query from the client container:

```shell
docker compose exec lancedb python -c "import duckdb, pathlib; print(duckdb.sql(pathlib.Path('queries/example.sql').read_text()).fetchall())"
```

## Teardown

```shell
docker compose down -v
```
