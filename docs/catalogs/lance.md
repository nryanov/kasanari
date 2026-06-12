# Lance Catalog

Kasanari's Lance support currently focuses on the **metadata API** and selected table DDL operations. It does **not** expose the full Lance data-plane API.

## Implemented versions

- Runtime library versions: `lance-namespace-core 0.7.2`, `lance-core 6.0.0`
- Kasanari API wrapper spec: `spec/lance/kasanari-lance-catalog-service.yaml`
- Upstream reference spec: `spec/lance/lance-openapi-0.7.2.yaml`

## Base URL pattern

Lance routes use:

- `http://<host>:<port>/lance/v1/...`

Catalog identity is encoded in the `id` path parameter (for example `catalog.namespace` or `catalog.namespace.table`).

## Modes

### INTERNAL

- Factory: `KasanariLanceCatalogFactory`
- Adapter: `DefaultLanceCatalogAdapter`
- Metadata storage: JDBC repositories in `modules/repository/repository-lance`

`INTERNAL` mode requires `implementation=kasanari` plus JDBC and warehouse properties.

### PROXY

- Factory: `ProxyLanceCatalogFactory`
- Delegate construction: `LanceNamespace.connect(...)`
- Adapter: `DefaultLanceCatalogAdapter`

`PROXY` mode commonly uses `implementation=dir` and delegates to an upstream Lance namespace backend.

## Registration examples

### PROXY example (`implementation=dir`)

```json
{
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
      "storage.aws_endpoint": "http://localhost:9000",
      "storage.aws_allow_http": "true",
      "storage.aws_virtual_hosted_style_request": "false",
      "storage.region": "us-east-1"
    }
  }
}
```

### INTERNAL example

```json
{
  "catalogId": "lance_internal",
  "catalogType": "LANCE",
  "mode": "INTERNAL",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "implementation": "kasanari",
      "uri": "jdbc:postgresql://localhost:5432/postgres",
      "kasanari.jdbc.user": "postgres",
      "kasanari.jdbc.password": "postgres",
      "lance.warehouse.location": "s3://warehouse",
      "lance.storage.aws_region": "us-east-1",
      "lance.storage.aws_access_key_id": "admin",
      "lance.storage.aws_secret_access_key": "password",
      "lance.storage.aws_endpoint": "http://localhost:9000",
      "lance.storage.allow_http": "true"
    }
  }
}
```

## Method implementation status

Status values:

- `Implemented`: endpoint is exposed and handled.
- `Partial`: endpoint exists but has known limitations.
- `Not implemented`: endpoint is not exposed by Kasanari.

### Implemented/partial endpoints

| Method | Path | Status | Notes |
|---|---|---|---|
| POST | `/lance/v1/namespace/{id}/create` | Implemented | |
| GET | `/lance/v1/namespace/{id}/list` | Implemented | |
| POST | `/lance/v1/namespace/{id}/describe` | Implemented | |
| POST | `/lance/v1/namespace/{id}/drop` | Implemented | |
| POST | `/lance/v1/namespace/{id}/exists` | Implemented | |
| GET | `/lance/v1/namespace/{id}/table/list` | Implemented | |
| POST | `/lance/v1/table/{id}/create` | Implemented | |
| POST | `/lance/v1/table/{id}/register` | Implemented | |
| POST | `/lance/v1/table/{id}/describe` | Implemented | |
| POST | `/lance/v1/table/{id}/exists` | Implemented | |
| POST | `/lance/v1/table/{id}/drop` | Implemented | |
| POST | `/lance/v1/table/{id}/deregister` | Implemented | |
| POST | `/lance/v1/table/{id}/restore` | Implemented | |
| POST | `/lance/v1/table/{id}/rename` | Implemented | |
| POST | `/lance/v1/table/{id}/alter_columns` | Implemented | |
| POST | `/lance/v1/table/{id}/drop_columns` | Implemented | |
| POST | `/lance/v1/table/{id}/add_columns` | Partial | Internal mode currently supports only non-virtual columns. |
| POST | `/lance/v1/table/{id}/declare` | Implemented | Maps to create-empty-table behavior. |

### Not implemented endpoint groups

| Methods | Path group | Status | Notes |
|---|---|---|---|
| GET | `/lance/v1/table` | Not implemented | Table listing/details endpoint from upstream spec not exposed. |
| POST | `/lance/v1/table/{id}/schema_metadata/update` | Not implemented | |
| POST | `/lance/v1/table/{id}/version/*` | Not implemented | Version lifecycle endpoints not exposed. |
| POST | `/lance/v1/table/version/batch-create` | Not implemented | |
| POST | `/lance/v1/table/batch-commit` | Not implemented | |
| POST | `/lance/v1/table/{id}/stats` | Not implemented | |
| POST | `/lance/v1/table/{id}/insert` | Not implemented | Data plane. |
| POST | `/lance/v1/table/{id}/merge_insert` | Not implemented | Data plane. |
| POST | `/lance/v1/table/{id}/update` | Not implemented | Data plane. |
| POST | `/lance/v1/table/{id}/delete` | Not implemented | Data plane. |
| POST | `/lance/v1/table/{id}/query` | Not implemented | Data plane. |
| POST | `/lance/v1/table/{id}/count_rows` | Not implemented | Data plane. |
| POST | `/lance/v1/table/{id}/explain_plan` | Not implemented | |
| POST | `/lance/v1/table/{id}/analyze_plan` | Not implemented | |
| POST | `/lance/v1/table/{id}/create_index` | Not implemented | |
| POST | `/lance/v1/table/{id}/create_scalar_index` | Not implemented | |
| POST | `/lance/v1/table/{id}/index/list` | Not implemented | |
| POST | `/lance/v1/table/{id}/index/{index_name}/stats` | Not implemented | |
| POST | `/lance/v1/table/{id}/index/{index_name}/drop` | Not implemented | |
| POST | `/lance/v1/table/{id}/tags/*` | Not implemented | Tag APIs are not exposed. |
| POST | `/lance/v1/transaction/{id}/*` | Not implemented | Transaction APIs are not exposed. |
| POST | `/lance/v1/oauth/tokens` | Not implemented | Token URL exists in security scheme, not routed by catalog API. |

## Important limitations

- Current implementation is metadata-focused; data plane APIs are intentionally absent.
- Alter-table support is partial (`add_columns` has constraints).
- `PROXY` mode with `implementation=dir` works for namespace/table metadata operations, with behavior constrained by upstream backend capabilities.
- Adapter refresh and replacement follows catalog metadata updates and refresh interval settings.
