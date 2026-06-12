# Lance Catalog

Kasanari currently focuses on Lance metadata APIs and selected DDL flows.

## Implemented versions

- Runtime library versions: `lance-namespace-core 0.7.2`, `lance-core 6.0.0`
- Kasanari API wrapper spec: `spec/lance/kasanari-lance-catalog-service.yaml`
- Upstream reference spec: `spec/lance/lance-openapi-0.7.2.yaml`

## Base URL pattern

- `http://<host>:<port>/lance/v1/...`
- Catalog identity is encoded in `id` path values (for example `catalog.namespace`).

## Implementations

| Implementation | Modes | Support status | Details |
|---|---|---|---|
| `kasanari` | `INTERNAL` | Verified | [Lance Kasanari](lance/kasanari.md) |

`PROXY` mode is also supported through `implementation=<backend>` (commonly `dir`), and is documented in the Lance Kasanari page as a runnable proxy setup example.

Catalogs are registered via `POST /management/v1/catalogs` with:

- `catalogType=LANCE`
- `mode=INTERNAL` or `mode=PROXY`
- `spec.fileIoProperties` and `spec.catalogProperties`

## Minimal runnable samples

### INTERNAL

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
      "lance.warehouse.location": "s3://warehouse"
    }
  }
}
```

### PROXY (`dir`)

```json
{
  "catalogId": "lance_proxy",
  "catalogType": "LANCE",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "implementation": "dir",
      "root": "s3://warehouse"
    }
  }
}
```

## API coverage summary

- `Implemented`: namespace lifecycle and most table metadata/DDL routes.
- `Partial`: `add_columns` (internal adapter currently supports non-virtual columns only).
- `Not implemented`: data-plane and advanced version/index/transaction endpoint groups.

See details in `spec/lance/kasanari-lance-catalog-service.yaml`.

## Important limitations

- Current implementation is metadata-focused; data-plane APIs are intentionally absent.
- `PROXY` mode behavior depends on the selected upstream backend implementation.
- Catalog loading requires `catalogProperties.implementation` in both modes.
- Adapter refresh follows metadata updates and `kasanari.catalog.refresh-interval` (default `30s`).
