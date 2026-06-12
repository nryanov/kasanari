# Paimon Catalog

Kasanari exposes Paimon REST APIs in `INTERNAL` and `PROXY` catalog modes.

## Implemented versions

- Runtime library version: Paimon `1.4.1`
- Kasanari API wrapper spec: `spec/paimon/kasanari-paimon-catalog-service.yaml`
- Upstream reference spec: `spec/paimon/paimon-openapi-1.4.1.yaml`

## Base URL pattern

- `http://<host>:<port>/paimon/v1/{catalogId}/...`
- `{catalogId}` is the Management API registration ID.

## Implementations

| Implementation | Modes | Support status | Details |
|---|---|---|---|
| `kasanari` | `INTERNAL` | Verified | [Paimon Kasanari](paimon/kasanari.md) |
| `filesystem` | `PROXY` | Verified | [Paimon Filesystem](paimon/filesystem.md) |
| `jdbc` | `PROXY` | Verified | [Paimon JDBC](paimon/jdbc.md) |
| `hive` | `PROXY` | Verified | [Paimon Hive](paimon/hive.md) |
| `rest` | `PROXY` | Experimental (not verified in this repo) | [Paimon REST](paimon/rest.md) |

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

## API coverage summary

- `Implemented`: most database, table, view, function, branch, tag, and consumer endpoints.
- `Partial`: a small set of endpoints such as auth/token table endpoints and some partition filter behavior.
- `Not implemented`: currently minimal; behavior can still vary by underlying proxied catalog type.

See details in `spec/paimon/kasanari-paimon-catalog-service.yaml`.

## Important limitations

- `token` and `auth` table endpoints are exposed but not fully implemented by the default adapter.
- Some partition filtering behavior differs in `INTERNAL` mode.
- In `PROXY` mode, behavior depends on the selected upstream Paimon catalog type.
- Catalog adapters are refreshed using `kasanari.catalog.refresh-interval` (default `30s`).
