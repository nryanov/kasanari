# Iceberg `jdbc` implementation

- Catalog type: `ICEBERG`
- Implementation mode: `PROXY`
- `catalogProperties.catalog-impl`: `org.apache.iceberg.jdbc.JdbcCatalog`

## INTERNAL catalog setup

Not applicable for this implementation. For Kasanari-owned metadata, use [Iceberg Kasanari](kasanari.md).

## PROXY catalog setup

### Minimal runnable config

```json
{
  "catalogId": "iceberg_proxy_jdbc",
  "catalogType": "ICEBERG",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "catalog-impl": "org.apache.iceberg.jdbc.JdbcCatalog",
      "uri": "jdbc:postgresql://localhost:5432/postgres",
      "jdbc.user": "postgres",
      "jdbc.password": "postgres",
      "warehouse": "s3a://warehouse"
    }
  }
}
```

### Full config sample

```json
{
  "catalogId": "iceberg_proxy_jdbc",
  "catalogType": "ICEBERG",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {
      "fs.s3a.endpoint": "http://localhost:9000",
      "fs.s3a.access.key": "admin",
      "fs.s3a.secret.key": "password",
      "fs.s3a.path.style.access": "true",
      "fs.s3a.connection.ssl.enabled": "false",
      "fs.s3a.aws.credentials.provider": "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider"
    },
    "catalogProperties": {
      "catalog-impl": "org.apache.iceberg.jdbc.JdbcCatalog",
      "uri": "jdbc:postgresql://localhost:5432/postgres",
      "jdbc.user": "postgres",
      "jdbc.password": "postgres",
      "jdbc.schema-version": "V1",
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

| config                             | default value           | meaning                                                                                            |
|------------------------------------|-------------------------|----------------------------------------------------------------------------------------------------|
| `catalog-impl`                     | none                    | Iceberg catalog implementation class. Must be `org.apache.iceberg.jdbc.JdbcCatalog` for this page. |
| `uri`                              | none                    | JDBC endpoint used by Iceberg JDBC catalog. Required.                                              |
| `jdbc.user`                        | none                    | JDBC catalog username.                                                                             |
| `jdbc.password`                    | none                    | JDBC catalog password.                                                                             |
| `jdbc.schema-version`              | implementation-specific | JDBC catalog schema mode (`V1` enables view support in tested setup).                              |
| `warehouse`                        | none                    | Warehouse root location. Required for practical use.                                               |
| `io-impl`                          | engine default          | Optional Iceberg `FileIO` implementation override.                                                 |
| `s3.endpoint`                      | none                    | S3 endpoint for `S3FileIO`.                                                                        |
| `s3.access-key-id`                 | none                    | S3 access key for `S3FileIO`.                                                                      |
| `s3.secret-access-key`             | none                    | S3 secret key for `S3FileIO`.                                                                      |
| `s3.path-style-access`             | none                    | Enables path-style S3 addressing.                                                                  |
| `fs.s3a.*` (in `fileIoProperties`) | none                    | Hadoop S3A settings passed into the delegated catalog environment.                                 |

