# Iceberg `hive` implementation

- Catalog type: `ICEBERG`
- Implementation mode: `PROXY`
- `catalogProperties.catalog-impl`: `org.apache.iceberg.hive.HiveCatalog`

## INTERNAL catalog setup

Not applicable for this implementation. For Kasanari-owned metadata, use [Iceberg Kasanari](kasanari.md).

## PROXY catalog setup

### Minimal runnable config

```json
{
  "catalogId": "iceberg_proxy_hive",
  "catalogType": "ICEBERG",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "catalog-impl": "org.apache.iceberg.hive.HiveCatalog",
      "uri": "thrift://localhost:9083",
      "warehouse": "s3a://warehouse"
    }
  }
}
```

### Full config sample

```json
{
  "catalogId": "iceberg_proxy_hive",
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
      "catalog-impl": "org.apache.iceberg.hive.HiveCatalog",
      "uri": "thrift://localhost:9083",
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

## Additional properties

| config                             | default value  | meaning                                                                                            |
|------------------------------------|----------------|----------------------------------------------------------------------------------------------------|
| `catalog-impl`                     | none           | Iceberg catalog implementation class. Must be `org.apache.iceberg.hive.HiveCatalog` for this page. |
| `uri`                              | none           | Hive Metastore Thrift URI. Required.                                                               |
| `warehouse`                        | none           | Warehouse root location. Required.                                                                 |
| `io-impl`                          | engine default | Optional Iceberg `FileIO` implementation override.                                                 |
| `s3.endpoint`                      | none           | S3 endpoint for `S3FileIO`.                                                                        |
| `s3.access-key-id`                 | none           | S3 access key for `S3FileIO`.                                                                      |
| `s3.secret-access-key`             | none           | S3 secret key for `S3FileIO`.                                                                      |
| `s3.path-style-access`             | none           | Enables path-style S3 addressing.                                                                  |
| `fs.s3a.*` (in `fileIoProperties`) | none           | Hadoop S3A settings passed into delegated catalog setup.                                           |

