# Paimon Catalog

Kasanari's Paimon support is **almost full REST coverage**: the API surface is broadly exposed, with a few operation-specific limitations in current adapters.

## Implemented versions

- Runtime library version: Paimon `1.4.1`
- Kasanari API wrapper spec: `spec/paimon/kasanari-paimon-catalog-service.yaml`
- Upstream reference spec: `spec/paimon/paimon-openapi-1.4.1.yaml`

## Base URL pattern

- `http://<host>:<port>/paimon/v1/{catalogId}/...`
- `{catalogId}` is the Management API registration ID.

## Modes

### INTERNAL

- Factory: `KasanariPaimonCatalogFactory`
- Adapter: `DefaultPaimonCatalogAdapter`
- Metadata storage: JDBC repositories in `modules/repository/repository-paimon`

Common required properties:

- `warehouse`
- `uri`
- `kasanari.jdbc.user`
- `kasanari.jdbc.password`
- `kasanari.catalog.key`

### PROXY

- Factory: `ProxyPaimonCatalogFactory`
- Delegate construction: `CatalogFactory.createCatalog(...)`

Common proxy catalog types include `filesystem`, `hive`, and `jdbc`.

## Registration examples

### INTERNAL example

```json
{
  "catalogId": "paimon_internal",
  "catalogType": "PAIMON",
  "mode": "INTERNAL",
  "spec": {
    "fileIoProperties": {
      "fs.s3a.access.key": "admin",
      "fs.s3a.secret.key": "password",
      "fs.s3a.impl": "org.apache.hadoop.fs.s3a.S3AFileSystem",
      "fs.s3a.path.style.access": "true",
      "fs.s3a.endpoint": "http://localhost:9000"
    },
    "catalogProperties": {
      "warehouse": "s3a://warehouse",
      "uri": "jdbc:postgresql://localhost:5432/postgres",
      "kasanari.jdbc.user": "postgres",
      "kasanari.jdbc.password": "postgres",
      "kasanari.catalog.key": "paimon_internal"
    }
  }
}
```

### PROXY example

```json
{
  "catalogId": "paimon_proxy",
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

## Method implementation status

Status values:

- `Implemented`: endpoint is exposed and handled.
- `Partial`: endpoint exists but has known mode-specific or adapter limitations.
- `Not implemented`: endpoint is not currently supported.

| Method | Path | Status | Notes |
|---|---|---|---|
| GET | `/paimon/v1/config` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases/{database}` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases/{database}` | Implemented | Alter database. |
| DELETE | `/paimon/v1/{prefix}/databases/{database}` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases/{database}/register` | Partial | Internal mode register behavior is limited. |
| GET | `/paimon/v1/{prefix}/databases/{database}/tables` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases/{database}/tables` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases/{database}/table-details` | Implemented | |
| GET | `/paimon/v1/{prefix}/tables` | Implemented | |
| GET | `/paimon/v1/{prefix}/tables/id/{tableId}` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases/{database}/tables/{table}` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases/{database}/tables/{table}` | Implemented | Alter table. |
| DELETE | `/paimon/v1/{prefix}/databases/{database}/tables/{table}` | Implemented | |
| POST | `/paimon/v1/{prefix}/tables/rename` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/commit` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/rollback` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/rollback-schema` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/token` | Partial | Currently returns unsupported in default adapter. |
| POST | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/auth` | Partial | Currently returns unsupported in default adapter. |
| GET | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/snapshot` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/snapshots/{version}` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/snapshots` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/partitions` | Partial | Pattern-based filtering has internal-mode limitations. |
| POST | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/partitions/mark` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/partitions/list-by-names` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/branches` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/branches` | Implemented | |
| DELETE | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/branches/{branch}` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/branches/{branch}/rename` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/branches/{branch}/forward` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/tags` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/tags` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/tags/{tag}` | Implemented | |
| DELETE | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/tags/{tag}` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/consumers` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases/{database}/tables/{table}/consumers/reset` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases/{database}/views` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases/{database}/views` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases/{database}/view-details` | Implemented | |
| GET | `/paimon/v1/{prefix}/views` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases/{database}/views/{view}` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases/{database}/views/{view}` | Implemented | |
| DELETE | `/paimon/v1/{prefix}/databases/{database}/views/{view}` | Implemented | |
| POST | `/paimon/v1/{prefix}/views/rename` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases/{database}/functions` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases/{database}/functions` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases/{database}/function-details` | Implemented | |
| GET | `/paimon/v1/{prefix}/functions` | Implemented | |
| GET | `/paimon/v1/{prefix}/databases/{database}/functions/{function}` | Implemented | |
| POST | `/paimon/v1/{prefix}/databases/{database}/functions/{function}` | Implemented | |
| DELETE | `/paimon/v1/{prefix}/databases/{database}/functions/{function}` | Implemented | |

## Important limitations

- `token` and `auth` table endpoints are exposed but not fully implemented by the default adapter.
- Some partition filtering behavior differs in `INTERNAL` mode.
- In `PROXY` mode, exact behavior depends on upstream Paimon catalog type/capabilities.
- Catalog adapters are refreshed using `kasanari.catalog.refresh-interval`.
