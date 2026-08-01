# Iceberg Catalog

Kasanari provides Iceberg catalog APIs on top of `INTERNAL` and `PROXY` catalog modes.

## Implemented versions

- Runtime library version: Iceberg `1.11.0`
- Kasanari API wrapper spec: `spec/iceberg/kasanari-iceberg-catalog-service.yaml`
- Upstream reference spec: `spec/iceberg/iceberg-openapi-1.11.0.yaml`

## Base URL pattern

- `http://<host>:<port>/iceberg/v1/{catalogId}/...`
- `{catalogId}` is the Management API registration ID.

## Implementations

| Implementation | Modes | Details |
|---|---|---|
| `kasanari` | `INTERNAL` | [Iceberg Kasanari](iceberg/kasanari.md) |
| `jdbc` | `PROXY` | [Iceberg JDBC](iceberg/jdbc.md) |
| `hive` | `PROXY` | [Iceberg Hive](iceberg/hive.md) |
| `hadoop` | `PROXY` | [Iceberg Hadoop](iceberg/hadoop.md) |
| `rest` | `PROXY` | [Iceberg REST](iceberg/rest.md) |

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

