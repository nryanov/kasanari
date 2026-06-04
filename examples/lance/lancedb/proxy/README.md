# LanceDB proxy example

Runs Kasanari with a LANCE catalog in `PROXY` mode and a Python `lancedb` client container.

## Prerequisites

- Docker and Docker Compose.
- Built Kasanari image:

```shell
./scripts/build-container-images.sh
```

## Startup

From repository root:

```shell
cd examples/lance/lancedb/proxy
docker compose up -d
```

## Register catalog command example

```shell
curl -s -X POST http://localhost:9090/management/v1/catalogs \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": "lance_proxy",
    "catalogType": "LANCE",
    "mode": "PROXY",
    "spec": {
      "fileIoProperties": {},
      "catalogProperties": {
        "implementation": "dir",
        "root": "s3://warehouse",
        "manifest_enabled": "false",
        "dir_listing_enabled": "true",
        "storage.aws_access_key_id": "admin",
        "storage.aws_secret_access_key": "password",
        "storage.aws_endpoint": "http://minio:9000",
        "storage.aws_allow_http": "true",
        "storage.aws_virtual_hosted_style_request": "false",
        "storage.access_key_id": "admin",
        "storage.secret_access_key": "password",
        "storage.endpoint": "http://minio:9000",
        "storage.allow_http": "true",
        "storage.virtual_hosted_style_request": "false",
        "storage.region": "us-east-1"
      }
    }
  }'
```

## Sample operations and expected outcomes

- `GET /management/v1/catalogs/LANCE/lance_proxy` returns `200`.
- `GET /management/v1/catalogs` includes `lance_proxy`.
- Run SQL smoke query from the client container:

```shell
docker compose exec lancedb python -c "import duckdb, pathlib; print(duckdb.sql(pathlib.Path('queries/01_smoke.sql').read_text()).fetchall())"
```

## Teardown

```shell
docker compose down -v
```
