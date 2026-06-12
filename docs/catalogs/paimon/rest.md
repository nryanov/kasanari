# Paimon `rest` implementation

- Catalog type: `PAIMON`
- Implementation mode: `PROXY`
- Intended upstream mode: REST metastore/catalog

This page provides a practical REST-oriented proxy shape to validate in your environment.

## INTERNAL catalog setup

Not applicable for this implementation. For Kasanari-owned metadata, use [Paimon Kasanari](kasanari.md).

## PROXY catalog setup

### Minimal runnable config

```json
{
  "catalogId": "paimon_proxy_rest",
  "catalogType": "PAIMON",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "type": "rest",
      "metastore": "rest",
      "uri": "https://paimon-catalog.example.com",
      "warehouse": "main"
    }
  }
}
```

### Full config sample

```json
{
  "catalogId": "paimon_proxy_rest",
  "catalogType": "PAIMON",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "type": "rest",
      "metastore": "rest",
      "uri": "https://paimon-catalog.example.com",
      "warehouse": "main",
      "token.provider": "bear",
      "token": "<bearer-token>",
      "s3.endpoint": "http://localhost:9000",
      "s3.access-key": "admin",
      "s3.secret-key": "password",
      "s3.path.style.access": "true"
    }
  }
}
```

## Additional properties

| config                  | default value      | meaning                                                              |
|-------------------------|--------------------|----------------------------------------------------------------------|
| `type`                  | provider-dependent | Catalog type selector; this page assumes REST usage.                 |
| `metastore`             | provider-dependent | REST metastore mode marker (`rest`) used by many Paimon REST setups. |
| `uri`                   | none               | Base URI of REST catalog server. Required.                           |
| `warehouse`             | provider-dependent | Catalog instance name or warehouse identifier (provider-specific).   |
| `token.provider`        | provider-dependent | Token provider type (`bear`, `dlf`, or provider-specific options).   |
| `token`                 | none               | Bearer token when using bearer-token authentication.                 |
| `dlf.access-key-id`     | none               | DLF auth access key ID (if DLF provider is used).                    |
| `dlf.access-key-secret` | none               | DLF auth access key secret (if DLF provider is used).                |
| `dlf.security-token`    | none               | Optional DLF temporary security token.                               |
| `s3.*`                  | none               | Optional object storage properties required by your backend.         |

