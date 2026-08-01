# Paimon Catalog

Kasanari exposes Paimon REST APIs in `INTERNAL` and `PROXY` catalog modes.

## Implemented versions

- Runtime library version: Paimon `1.4.2`
- Kasanari API wrapper spec: `spec/paimon/kasanari-paimon-catalog-service.yaml`
- Upstream reference spec: `spec/paimon/paimon-openapi-1.4.1.yaml`

## Base URL pattern

- `http://<host>:<port>/paimon/v1/{catalogId}/...`
- `{catalogId}` is the Management API registration ID.

## Implementations

| Implementation | Modes | Details |
|---|---|---|
| `kasanari` | `INTERNAL` | [Paimon Kasanari](paimon/kasanari.md) |
| `filesystem` | `PROXY` | [Paimon Filesystem](paimon/filesystem.md) |
| `jdbc` | `PROXY` | [Paimon JDBC](paimon/jdbc.md) |
| `hive` | `PROXY` | [Paimon Hive](paimon/hive.md) |
| `rest` | `PROXY` | [Paimon REST](paimon/rest.md) |

Catalogs are registered via `POST /management/v1/catalogs` with:

- `catalogType=PAIMON`
- `mode=INTERNAL` or `mode=PROXY`
- `spec.fileIoProperties` and `spec.catalogProperties`

## Minimal runnable samples

### INTERNAL

```json
{
  "catalogId": "paimon_internal",
  "catalogType": "PAIMON",
  "mode": "INTERNAL",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "warehouse": "s3a://warehouse",
      "uri": "jdbc:postgresql://localhost:5432/postgres",
      "kasanari.jdbc.user": "postgres",
      "kasanari.jdbc.password": "postgres"
    }
  }
}
```

### PROXY

```json
{
  "catalogId": "paimon_proxy_filesystem",
  "catalogType": "PAIMON",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {
      "fs.s3a.access.key": "admin",
      "fs.s3a.secret.key": "password",
      "fs.s3a.impl": "org.apache.hadoop.fs.s3a.S3AFileSystem",
      "fs.s3a.path.style.access": "true",
      "fs.s3a.endpoint": "http://localhost:9000"
    },
    "catalogProperties": {
      "type": "filesystem",
      "warehouse": "s3a://warehouse"
    }
  }
}
```

