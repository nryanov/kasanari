# Iceberg `kasanari` implementation

- Catalog type: `ICEBERG`
- Implementation mode: `INTERNAL`
- Factory: `KasanariIcebergCatalogFactory`

## INTERNAL catalog setup

### Minimal runnable config

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

### Full config sample

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
      "kasanari.jdbc.connection-pool.initial-size": "2",
      "kasanari.jdbc.connection-pool.min-size": "1",
      "kasanari.jdbc.connection-pool.max-size": "5",
      "kasanari.jdbc.connection-pool.max-lifetime.millis": "0",
      "warehouse": "s3a://warehouse",
      "io-impl": "org.apache.iceberg.aws.s3.S3FileIO",
      "s3.endpoint": "http://localhost:9000",
      "s3.access-key-id": "admin",
      "s3.secret-access-key": "password",
      "s3.path-style-access": "true"
    }
  }
}
```

## PROXY catalog setup

Not applicable for this implementation. Use one of the `PROXY` pages:

- [Iceberg JDBC](jdbc.md)
- [Iceberg Hive](hive.md)
- [Iceberg Hadoop](hadoop.md)
- [Iceberg REST](rest.md)

## Additional properties

| config | default value | meaning |
|---|---|---|
| `uri` | none | JDBC URL for Kasanari metadata repositories. Required. |
| `kasanari.jdbc.user` | none | JDBC username for metadata DB. Required. |
| `kasanari.jdbc.password` | none | JDBC password for metadata DB. Required. |
| `kasanari.jdbc.connection-pool.initial-size` | `2` | Initial JDBC pool size. |
| `kasanari.jdbc.connection-pool.min-size` | `1` | Minimum JDBC pool size. |
| `kasanari.jdbc.connection-pool.max-size` | `5` | Maximum JDBC pool size. |
| `kasanari.jdbc.connection-pool.max-lifetime.millis` | `0` | Max JDBC connection lifetime in milliseconds (`0` means no lifetime limit). |
| `warehouse` | none | Root location for table/view metadata and data paths. Required. |
| `io-impl` | `org.apache.iceberg.hadoop.HadoopFileIO` | Iceberg `FileIO` implementation. |
| `s3.endpoint` | none | S3 endpoint for `S3FileIO` (for example MinIO). |
| `s3.access-key-id` | none | S3 access key for `S3FileIO`. |
| `s3.secret-access-key` | none | S3 secret key for `S3FileIO`. |
| `s3.path-style-access` | none | Enables path-style S3 addressing when needed. |

