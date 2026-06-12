# Quickstart

This quickstart brings up a complete baseline environment and registers an Iceberg catalog.

Baseline stack:

- PostgreSQL metadata DB
- MinIO object storage
- Kasanari server
- Iceberg catalog registration in `INTERNAL` mode

## Prerequisites

- Java `21`
- Docker + Docker Compose
- `curl` and `jq`

## 1) Build server artifacts

From repository root:

```bash
./gradlew assemble --no-daemon
```

## 2) Start baseline infrastructure (DB + object storage)

```bash
docker compose -f development/docker-compose.yml up -d
```

Services started by this compose file:

- MinIO API: `http://localhost:9000`
- MinIO Console: `http://localhost:9001`
- PostgreSQL: `localhost:5432`

## 3) Start Kasanari

Run server with the default local profile (`kasanari.authentication.type=none`):

```bash
./gradlew :modules:server:quarkusDev -Dquarkus.profile=dev
```

## 4) Register an Iceberg baseline catalog

In another shell, create an `ICEBERG` catalog in `INTERNAL` mode:

```bash
curl -sS -X POST "http://localhost:9090/management/v1/catalogs" \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": "iceberg_baseline",
    "catalogType": "ICEBERG",
    "mode": "INTERNAL",
    "spec": {
      "fileIoProperties": {},
      "catalogProperties": {
        "uri": "jdbc:postgresql://localhost:5432/postgres",
        "kasanari.jdbc.user": "postgres",
        "kasanari.jdbc.password": "postgres",
        "warehouse": "s3a://warehouse",
        "io-impl": "org.apache.iceberg.aws.s3.S3FileIO",
        "s3.endpoint": "http://localhost:9000",
        "s3.access-key-id": "admin",
        "s3.secret-access-key": "password",
        "s3.path-style-access": "true",
        "s3.client-factory": "kasanari.catalog.iceberg.s3.NoneRegionS3FileIOAwsClientFactory"
      }
    }
  }' | jq .
```

Expected: response with `catalogId: iceberg_baseline` and HTTP `201`.

## 5) Verify end-to-end

### Server health and docs

```bash
curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:9090/q/health"
```

Expected: `200`

- Swagger UI: `http://localhost:9090/docs`

### Management lookup

```bash
curl -sS "http://localhost:9090/management/v1/catalogs/ICEBERG/iceberg_baseline" | jq .
```

### Iceberg REST endpoint smoke check

```bash
curl -sS "http://localhost:9090/iceberg/v1/iceberg_baseline/namespaces" | jq .
```

This confirms that the catalog is registered and reachable through the Iceberg REST API.

## Optional next steps

- Run a full query engine stack: `examples/trino/README.md`
- Run Spark notebooks: `examples/spark/README.md`
- Enable LDAP or OIDC providers: see security pages and `examples/authentication/ldap/README.md`, `examples/authentication/oidc/README.md`
- Explore all available engine/client guides: `integrations/index.md`
