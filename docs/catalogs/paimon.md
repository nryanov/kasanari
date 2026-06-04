# Paimon Catalog

Kasanari provides Paimon through dedicated internal and proxy factories.

## Modes

## Internal mode

- Factory: `KasanariPaimonCatalogFactory`
- Adapter: `DefaultPaimonCatalogAdapter`
- Backing metadata: JDBC repositories in `modules/repository/repository-paimon/*`

Required property:

- `warehouse`

Common internal properties:

- `uri`
- `kasanari.jdbc.user`
- `kasanari.jdbc.password`
- `kasanari.catalog.key`

## Proxy mode

- Factory: `ProxyPaimonCatalogFactory`
- Delegate creation: `CatalogFactory.createCatalog(...)`

Use `PROXY` to route to external Paimon-compatible catalogs (for example `filesystem`, `hive`, `jdbc`).

## Registration payloads

### Internal example

```json
{
  "catalogId": "paimon-internal",
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
      "kasanari.catalog.key": "paimon-internal"
    }
  }
}
```

### Proxy example

```json
{
  "catalogId": "paimon-proxy",
  "catalogType": "PAIMON",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {
      "fs.s3a.access.key": "admin",
      "fs.s3a.secret.key": "password",
      "fs.s3a.impl": "org.apache.hadoop.fs.s3a.S3AFileSystem",
      "fs.s3a.path.style.access": "true",
      "fs.s3a.endpoint": "http://localhost:9000"
    },
    "catalogProperties": {
      "type": "filesystem",
      "warehouse": "s3a://warehouse"
    }
  }
}
```

## Operational notes

- Like other catalogs, Paimon adapters are refreshed from management metadata every `kasanari.catalog.refresh-interval`.
- Missing `warehouse` in internal mode fails adapter creation.
