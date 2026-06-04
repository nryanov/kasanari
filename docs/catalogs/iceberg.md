# Iceberg Catalog

Kasanari serves Iceberg through adapters selected by catalog mode.

## Modes

## Internal mode

- Factory: `KasanariIcebergCatalogFactory`
- Adapter: `KasanariIcebergCatalogAdapter`
- Backing metadata: JDBC repositories in `modules/repository/repository-iceberg/*`

Use `INTERNAL` when you want Kasanari-managed metadata and behavior.

## Proxy mode

- Factory: `ProxyIcebergCatalogFactory`
- Delegate creation: `CatalogUtil.buildIcebergCatalog(...)`
- Adapter: `DefaultIcebergCatalogAdapter`

Use `PROXY` when you want Kasanari to front an existing Iceberg catalog implementation.

## Registration payloads

Catalogs are registered via Management API `POST /management/v1/catalogs`.

### Internal example

```json
{
  "catalogId": "iceberg-internal",
  "catalogType": "ICEBERG",
  "mode": "INTERNAL",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "uri": "jdbc:postgresql://localhost:5432/postgres",
      "kasanari.jdbc.user": "postgres",
      "kasanari.jdbc.password": "postgres",
      "warehouse": "s3a://warehouse",
      "io-impl": "org.apache.iceberg.aws.s3.S3FileIO",
      "s3.endpoint": "http://localhost:9000",
      "s3.access-key-id": "admin",
      "s3.secret-access-key": "password",
      "s3.path-style-access": "true",
      "s3.client-factory": "kasanari.catalog.iceberg.s3.NoneRegionS3FileIOAwsClientFactory"
    }
  }
}
```

### Proxy example

```json
{
  "catalogId": "iceberg-proxy",
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
      "catalog-impl": "org.apache.iceberg.hadoop.HadoopCatalog",
      "warehouse": "s3a://warehouse",
      "io-impl": "org.apache.iceberg.aws.s3.S3FileIO"
    }
  }
}
```

## Operational notes

- Catalogs are reloaded from management metadata on `kasanari.catalog.refresh-interval` (default `30s`).
- Re-register/update metadata through management endpoints to rotate config without restarting the server.
