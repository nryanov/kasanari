# Iceberg Catalog

Kasanari provides Iceberg catalog APIs on top of `INTERNAL` and `PROXY` catalog modes.

## Implemented versions

- Runtime library version: Iceberg `1.10.1`
- Kasanari API wrapper spec: `spec/iceberg/kasanari-iceberg-catalog-service.yaml`
- Upstream reference spec: `spec/iceberg/iceberg-openapi-1.10.1.yaml`

## Base URL pattern

- `http://<host>:<port>/iceberg/v1/{catalogId}/...`
- `{catalogId}` is the Management API registration ID.

## Implementations

| Implementation | Modes | Support status | Details |
|---|---|---|---|
| `kasanari` | `INTERNAL` | Verified | [Iceberg Kasanari](iceberg/kasanari.md) |
| `jdbc` | `PROXY` | Verified | [Iceberg JDBC](iceberg/jdbc.md) |
| `hive` | `PROXY` | Verified | [Iceberg Hive](iceberg/hive.md) |
| `hadoop` | `PROXY` | Verified | [Iceberg Hadoop](iceberg/hadoop.md) |
| `rest` | `PROXY` | Experimental (not verified in this repo) | [Iceberg REST](iceberg/rest.md) |

Catalogs are registered via `POST /management/v1/catalogs` with:

- `catalogType=ICEBERG`
- `mode=INTERNAL` or `mode=PROXY`
- `spec.fileIoProperties` and `spec.catalogProperties`

## Minimal runnable samples

### INTERNAL

```json
{
  "catalogId": "iceberg_internal",
  "catalogType": "ICEBERG",
  "mode": "INTERNAL",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "uri": "jdbc:postgresql://localhost:5432/postgres",
      "kasanari.jdbc.user": "postgres",
      "kasanari.jdbc.password": "postgres",
      "warehouse": "s3a://warehouse"
    }
  }
}
```

### PROXY

```json
{
  "catalogId": "iceberg_proxy_hadoop",
  "catalogType": "ICEBERG",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {
      "fs.s3a.endpoint": "http://localhost:9000",
      "fs.s3a.access.key": "admin",
      "fs.s3a.secret.key": "password",
      "fs.s3a.path.style.access": "true",
      "fs.s3a.connection.ssl.enabled": "false"
    },
    "catalogProperties": {
      "catalog-impl": "org.apache.iceberg.hadoop.HadoopCatalog",
      "warehouse": "s3a://warehouse"
    }
  }
}
```

## API coverage summary

- `Implemented`: most namespace, table, and view endpoints.
- `Partial`: a small set of endpoints, including table load optional params and metrics behavior.
- `Not implemented`: credentials and scan-planning endpoints (`/plan`, `/tasks`), and catalog OAuth token endpoint.

See details in `spec/iceberg/kasanari-iceberg-catalog-service.yaml`.

## Important limitations

- Scan planning (`/plan`, `/tasks`) and credentials endpoints are intentionally not exposed.
- OAuth token issuance endpoint is not implemented in catalog API surface.
- Catalog adapters are refreshed based on metadata updates (`kasanari.catalog.refresh-interval`, default `30s`).
