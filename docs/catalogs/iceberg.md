# Iceberg Catalog

Kasanari's Iceberg support is **almost full REST coverage** of Iceberg REST Catalog API with a few intentionally excluded areas.

## Implemented versions

- Runtime library version: Iceberg `1.10.1`
- Kasanari API wrapper spec: `spec/iceberg/kasanari-iceberg-catalog-service.yaml`
- Upstream reference spec: `spec/iceberg/iceberg-openapi-1.10.1.yaml`

## Base URL pattern

- `http://<host>:<port>/iceberg/v1/{catalogId}/...`
- `{catalogId}` is the Management API registration ID.

## Modes

### INTERNAL

- Factory: `KasanariIcebergCatalogFactory`
- Adapter: `KasanariIcebergCatalogAdapter`
- Metadata storage: JDBC repositories in `modules/repository/repository-iceberg`

Use `INTERNAL` for Kasanari-owned catalog metadata and behavior.

### PROXY

- Factory: `ProxyIcebergCatalogFactory`
- Delegate construction: `CatalogUtil.buildIcebergCatalog(...)`
- Adapter: `DefaultIcebergCatalogAdapter`

Use `PROXY` when Kasanari should front an existing Iceberg catalog.

## Registration examples

Catalogs are registered with `POST /management/v1/catalogs`.

### INTERNAL example

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

### PROXY example

```json
{
  "catalogId": "iceberg_proxy",
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
      "warehouse": "s3a://warehouse",
      "io-impl": "org.apache.iceberg.aws.s3.S3FileIO"
    }
  }
}
```

## Method implementation status

Status values:

- `Implemented`: endpoint is exposed and handled.
- `Partial`: endpoint exists but has known limitations.
- `Not implemented`: endpoint exists in upstream ecosystem but not exposed by Kasanari.

| Method | Path | Status | Notes |
|---|---|---|---|
| GET | `/iceberg/v1/config` | Implemented | Configuration negotiation endpoint. |
| GET | `/iceberg/v1/{prefix}/namespaces` | Implemented | |
| POST | `/iceberg/v1/{prefix}/namespaces` | Implemented | |
| GET | `/iceberg/v1/{prefix}/namespaces/{namespace}` | Implemented | |
| HEAD | `/iceberg/v1/{prefix}/namespaces/{namespace}` | Implemented | Namespace exists check. |
| DELETE | `/iceberg/v1/{prefix}/namespaces/{namespace}` | Implemented | |
| POST | `/iceberg/v1/{prefix}/namespaces/{namespace}/properties` | Implemented | |
| GET | `/iceberg/v1/{prefix}/namespaces/{namespace}/tables` | Implemented | |
| POST | `/iceberg/v1/{prefix}/namespaces/{namespace}/tables` | Implemented | Create table. |
| POST | `/iceberg/v1/{prefix}/namespaces/{namespace}/register` | Implemented | Register existing table. |
| GET | `/iceberg/v1/{prefix}/namespaces/{namespace}/tables/{table}` | Partial | Some optional request parameters are currently not propagated. |
| HEAD | `/iceberg/v1/{prefix}/namespaces/{namespace}/tables/{table}` | Implemented | Table exists check. |
| POST | `/iceberg/v1/{prefix}/namespaces/{namespace}/tables/{table}` | Implemented | Commit table update. |
| DELETE | `/iceberg/v1/{prefix}/namespaces/{namespace}/tables/{table}` | Implemented | Drop table. |
| POST | `/iceberg/v1/{prefix}/tables/rename` | Implemented | |
| POST | `/iceberg/v1/{prefix}/transactions/commit` | Implemented | |
| GET | `/iceberg/v1/{prefix}/namespaces/{namespace}/views` | Implemented | |
| POST | `/iceberg/v1/{prefix}/namespaces/{namespace}/views` | Implemented | |
| GET | `/iceberg/v1/{prefix}/namespaces/{namespace}/views/{view}` | Implemented | |
| HEAD | `/iceberg/v1/{prefix}/namespaces/{namespace}/views/{view}` | Implemented | View exists check. |
| POST | `/iceberg/v1/{prefix}/namespaces/{namespace}/views/{view}` | Implemented | Commit view update. |
| DELETE | `/iceberg/v1/{prefix}/namespaces/{namespace}/views/{view}` | Implemented | |
| POST | `/iceberg/v1/{prefix}/views/rename` | Implemented | |
| POST | `/iceberg/v1/{prefix}/namespaces/{namespace}/tables/{table}/metrics` | Partial | Routed, but current default adapter handling is minimal/no-op. |
| GET | `/iceberg/v1/{prefix}/namespaces/{namespace}/tables/{table}/credentials` | Not implemented | Explicitly excluded in Kasanari spec. |
| POST | `/iceberg/v1/{prefix}/namespaces/{namespace}/tables/{table}/plan` | Not implemented | Explicitly excluded in Kasanari spec. |
| GET | `/iceberg/v1/{prefix}/namespaces/{namespace}/tables/{table}/plan/{plan-id}` | Not implemented | Explicitly excluded in Kasanari spec. |
| DELETE | `/iceberg/v1/{prefix}/namespaces/{namespace}/tables/{table}/plan/{plan-id}` | Not implemented | Explicitly excluded in Kasanari spec. |
| POST | `/iceberg/v1/{prefix}/namespaces/{namespace}/tables/{table}/tasks` | Not implemented | Explicitly excluded in Kasanari spec. |
| POST | `/iceberg/v1/oauth/tokens` | Not implemented | Token URL exists in security scheme, not routed by catalog API. |

## Important limitations

- Scan planning (`/plan`, `/tasks`) and credentials endpoints are intentionally not exposed.
- OAuth token issuance endpoint is not implemented in catalog API surface.
- Catalog adapters are refreshed based on metadata updates (`kasanari.catalog.refresh-interval`).
