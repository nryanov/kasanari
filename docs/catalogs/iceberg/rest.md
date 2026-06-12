# Iceberg `rest` implementation

- Catalog type: `ICEBERG`
- Implementation mode: `PROXY`
- `catalogProperties.catalog-impl`: `org.apache.iceberg.rest.RESTCatalog`

This page documents a practical Iceberg REST proxy setup shape for Kasanari. Validate options against your target REST catalog provider (for example Polaris or Gravitino-compatible services).

## INTERNAL catalog setup

Not applicable for this implementation. For Kasanari-owned metadata, use [Iceberg Kasanari](kasanari.md).

## PROXY catalog setup

### Minimal runnable config

```json
{
  "catalogId": "iceberg_proxy_rest",
  "catalogType": "ICEBERG",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "catalog-impl": "org.apache.iceberg.rest.RESTCatalog",
      "uri": "https://polaris.example.com/api/catalog",
      "warehouse": "main"
    }
  }
}
```

### Full config sample

```json
{
  "catalogId": "iceberg_proxy_rest",
  "catalogType": "ICEBERG",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "catalog-impl": "org.apache.iceberg.rest.RESTCatalog",
      "uri": "https://polaris.example.com/api/catalog",
      "warehouse": "main",
      "credential": "client_id:client_secret",
      "scope": "catalog",
      "oauth2-server-uri": "https://polaris.example.com/api/catalog/v1/oauth/tokens",
      "token-exchange-enabled": "true",
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

| config                   | default value              | meaning                                                                                            |
|--------------------------|----------------------------|----------------------------------------------------------------------------------------------------|
| `catalog-impl`           | none                       | Iceberg catalog implementation class. Must be `org.apache.iceberg.rest.RESTCatalog` for this page. |
| `uri`                    | none                       | Base URL of the upstream Iceberg REST catalog service. Required.                                   |
| `warehouse`              | none                       | Target warehouse or catalog namespace identifier. Required in most deployments.                    |
| `credential`             | none                       | OAuth client credentials in `client_id:client_secret` format.                                      |
| `token`                  | none                       | Optional bearer token; alternative to `credential`.                                                |
| `oauth2-server-uri`      | provider-dependent         | OAuth token endpoint URI (required when catalog and auth endpoints differ).                        |
| `scope`                  | `catalog` (common default) | Optional OAuth scope value.                                                                        |
| `token-exchange-enabled` | `true` (common default)    | Enables token exchange flow when supported by client/provider.                                     |
| `io-impl`                | engine default             | Optional Iceberg `FileIO` implementation override.                                                 |
| `s3.*`                   | none                       | Storage settings needed when tables are backed by S3-compatible object storage.                    |

