# Iceberg `hadoop` implementation

- Catalog type: `ICEBERG`
- Implementation mode: `PROXY`
- `catalogProperties.catalog-impl`: `org.apache.iceberg.hadoop.HadoopCatalog`

## INTERNAL catalog setup

Not applicable for this implementation. For Kasanari-owned metadata, use [Iceberg Kasanari](kasanari.md).

## PROXY catalog setup

### Minimal runnable config

```json
{
  "catalogId": "iceberg_proxy_hadoop",
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
      "catalog-impl": "org.apache.iceberg.hadoop.HadoopCatalog",
      "warehouse": "s3a://warehouse"
    }
  }
}
```

### Full config sample

```json
{
  "catalogId": "iceberg_proxy_hadoop",
  "catalogType": "ICEBERG",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {
      "fs.s3a.endpoint": "http://localhost:9000",
      "fs.s3a.access.key": "admin",
      "fs.s3a.secret.key": "password",
      "fs.s3a.path.style.access": "true",
      "fs.s3a.connection.ssl.enabled": "false",
      "fs.s3a.aws.credentials.provider": "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider",
      "fs.s3a.aws.region": "us-east-1"
    },
    "catalogProperties": {
      "catalog-impl": "org.apache.iceberg.hadoop.HadoopCatalog",
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

| config                            | default value          | meaning                                                                                                |
|-----------------------------------|------------------------|--------------------------------------------------------------------------------------------------------|
| `catalog-impl`                    | none                   | Iceberg catalog implementation class. Must be `org.apache.iceberg.hadoop.HadoopCatalog` for this page. |
| `warehouse`                       | none                   | Warehouse root location. Required.                                                                     |
| `io-impl`                         | engine default         | Optional Iceberg `FileIO` implementation override.                                                     |
| `s3.endpoint`                     | none                   | S3 endpoint for `S3FileIO`.                                                                            |
| `s3.access-key-id`                | none                   | S3 access key for `S3FileIO`.                                                                          |
| `s3.secret-access-key`            | none                   | S3 secret key for `S3FileIO`.                                                                          |
| `s3.path-style-access`            | none                   | Enables path-style S3 addressing.                                                                      |
| `fs.s3a.endpoint`                 | none                   | Hadoop S3A endpoint for delegated Hadoop configuration.                                                |
| `fs.s3a.access.key`               | none                   | Hadoop S3A access key.                                                                                 |
| `fs.s3a.secret.key`               | none                   | Hadoop S3A secret key.                                                                                 |
| `fs.s3a.path.style.access`        | none                   | Enables S3A path-style addressing.                                                                     |
| `fs.s3a.connection.ssl.enabled`   | implementation default | Enables/disables TLS for S3A endpoint.                                                                 |
| `fs.s3a.aws.credentials.provider` | implementation default | Optional AWS credentials provider class for S3A.                                                       |

