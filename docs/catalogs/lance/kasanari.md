# Lance `kasanari` implementation

- Catalog type: `LANCE`
- Factory (internal): `KasanariLanceCatalogFactory`
- Factory (proxy): `ProxyLanceCatalogFactory`

## INTERNAL catalog setup

### Minimal runnable config

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

### Full config sample

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
      "kasanari.jdbc.connection-pool.initial-size": "2",
      "kasanari.jdbc.connection-pool.min-size": "1",
      "kasanari.jdbc.connection-pool.max-size": "5",
      "kasanari.jdbc.connection-pool.max-lifetime.millis": "0",
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

### JDBC backend

Set `kasanari.repository.backend` to `postgres` (default) or `yugabyte`.

INTERNAL rows are isolated by `catalog_name`. Because Lance REST has no catalog-name parameter, Kasanari injects `kasanari.catalog.name=<management catalogId>` when unset. On YugabyteDB the column is also the hash-shard key so one catalog stays on one tablet. See [YugabyteDB backend](../../operations/yugabyte.md).

## PROXY catalog setup

`implementation=dir` is the common proxy setup used in local examples.

### Minimal runnable config

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

### Full config sample

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

## Additional properties

| config                                              | default value           | meaning                                                                                                 |
|-----------------------------------------------------|-------------------------|---------------------------------------------------------------------------------------------------------|
| `implementation`                                    | none                    | Required in both modes. `kasanari` for internal metadata catalog, or proxy backend value such as `dir`. |
| `uri`                                               | none                    | JDBC URL for internal metadata repositories. Required in internal mode.                                 |
| `kasanari.jdbc.user`                                | none                    | JDBC username for internal mode metadata DB. Required in internal mode.                                 |
| `kasanari.jdbc.password`                            | none                    | JDBC password for internal mode metadata DB. Required in internal mode.                                 |
| `kasanari.jdbc.connection-pool.initial-size`        | `2`                     | Initial JDBC pool size (internal mode).                                                                 |
| `kasanari.jdbc.connection-pool.min-size`            | `1`                     | Minimum JDBC pool size (internal mode).                                                                 |
| `kasanari.jdbc.connection-pool.max-size`            | `5`                     | Maximum JDBC pool size (internal mode).                                                                 |
| `kasanari.jdbc.connection-pool.max-lifetime.millis` | `0`                     | Max JDBC connection lifetime in milliseconds (`0` means no lifetime limit).                             |
| `lance.warehouse.location`                          | none                    | Default warehouse location for internal tables. Required in internal mode.                              |
| `lance.storage.*`                                   | none                    | Internal mode storage options forwarded to Lance dataset operations.                                    |
| `root`                                              | none                    | Proxy `dir` backend root location.                                                                      |
| `storage.*`                                         | none                    | Proxy backend storage options for `LanceNamespace.connect(...)`.                                        |
| `manifest_enabled`                                  | implementation-specific | Proxy `dir` backend setting to control manifests behavior.                                              |
| `dir_listing_enabled`                               | implementation-specific | Proxy `dir` backend setting for namespace/table listing behavior.                                       |

