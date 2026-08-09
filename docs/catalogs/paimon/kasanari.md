# Paimon `kasanari` implementation

- Catalog type: `PAIMON`
- Implementation mode: `INTERNAL`
- Factory: `KasanariPaimonCatalogFactory`

## INTERNAL catalog setup

### Minimal runnable config

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

### Full config sample

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
      "kasanari.jdbc.connection-pool.initial-size": "2",
      "kasanari.jdbc.connection-pool.min-size": "1",
      "kasanari.jdbc.connection-pool.max-size": "5",
      "kasanari.jdbc.connection-pool.max-lifetime.millis": "0",
      "kasanari.catalog.name": "paimon_internal"
    }
  }
}
```

## PROXY catalog setup

Not applicable for this implementation. Use one of the `PROXY` pages:

- [Paimon Filesystem](filesystem.md)
- [Paimon JDBC](jdbc.md)
- [Paimon Hive](hive.md)
- [Paimon REST](rest.md)

## Additional properties

| config                                              | default value | meaning                                                                     |
|-----------------------------------------------------|---------------|-----------------------------------------------------------------------------|
| `warehouse`                                         | none          | Paimon warehouse path. Required.                                            |
| `uri`                                               | none          | JDBC URL for Kasanari metadata repositories. Required.                      |
| `kasanari.jdbc.user`                                | none          | JDBC username for metadata DB. Required.                                    |
| `kasanari.jdbc.password`                            | none          | JDBC password for metadata DB. Required.                                    |
| `kasanari.catalog.name`                             | `default`     | Management catalog id used to isolate metadata records in JDBC repositories. Injected automatically when unset. |
| `kasanari.jdbc.connection-pool.initial-size`        | `2`           | Initial JDBC pool size.                                                     |
| `kasanari.jdbc.connection-pool.min-size`            | `1`           | Minimum JDBC pool size.                                                     |
| `kasanari.jdbc.connection-pool.max-size`            | `5`           | Maximum JDBC pool size.                                                     |
| `kasanari.jdbc.connection-pool.max-lifetime.millis` | `0`           | Max JDBC connection lifetime in milliseconds (`0` means no lifetime limit). |
| `fs.s3a.*` (in `fileIoProperties`)                  | none          | Hadoop S3A options used by Paimon FileIO for S3-compatible storage.         |

