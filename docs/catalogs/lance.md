# Lance Catalog

Kasanari supports Lance catalogs in internal and proxy modes.

## Modes

## Internal mode

- Factory: `KasanariLanceCatalogFactory`
- Adapter: `DefaultLanceCatalogAdapter`
- Backing metadata: JDBC repositories in `modules/repository/repository-lance/*`

Internal mode merges `fileIoProperties` and `catalogProperties` before initialization.

## Proxy mode

- Factory: `ProxyLanceCatalogFactory`
- Delegate creation: `LanceNamespace.connect(...)`
- Adapter: `DefaultLanceCatalogAdapter`

Required property in both modes:

- `implementation`

If `implementation` is missing, router creation is skipped and logged as error.

## Registration payloads

### Proxy example

```json
{
  "catalogId": "lance-proxy",
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

### Internal example

```json
{
  "catalogId": "lance-internal",
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

## Operational notes

- Lance adapters are hot-reloaded on metadata version change.
- Router closes old adapters on replace/remove to release resources safely.
