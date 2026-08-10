# Paimon `jdbc` implementation

- Catalog type: `PAIMON`
- Implementation mode: `PROXY`
- `catalogProperties.type`: `jdbc`

## INTERNAL catalog setup

Not applicable for this implementation. For Kasanari-owned metadata, use [Paimon Kasanari](kasanari.md).

## PROXY catalog setup

`catalog-key` defaults to `jdbc`. When several PROXY JDBC catalogs share one JDBC metastore, set a distinct `catalog-key` per catalog.

### Minimal runnable config

```json
{
  "catalogId": "paimon_proxy_jdbc",
  "catalogType": "PAIMON",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "type": "jdbc",
      "warehouse": "s3a://warehouse",
      "jdbc-url": "jdbc:postgresql://localhost:5432/postgres",
      "jdbc-user": "postgres",
      "jdbc-password": "postgres",
      "catalog-key": "paimon_proxy_jdbc"
    }
  }
}
```

### Full config sample

```json
{
  "catalogId": "paimon_proxy_jdbc",
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
      "type": "jdbc",
      "warehouse": "s3a://warehouse",
      "jdbc-url": "jdbc:postgresql://localhost:5432/postgres",
      "jdbc-user": "postgres",
      "jdbc-password": "postgres",
      "jdbc-driver": "org.postgresql.Driver",
      "jdbc-table-prefix": "paimon_",
      "catalog-key": "paimon_proxy_jdbc"
    }
  }
}
```

### Shared JDBC metastore

```json
{
  "catalogId": "paimon_proxy_jdbc_a",
  "catalogType": "PAIMON",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "type": "jdbc",
      "warehouse": "s3a://warehouse-a",
      "jdbc-url": "jdbc:postgresql://localhost:5432/postgres",
      "jdbc-user": "postgres",
      "jdbc-password": "postgres",
      "catalog-key": "catalog_a"
    }
  }
}
```

```json
{
  "catalogId": "paimon_proxy_jdbc_b",
  "catalogType": "PAIMON",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "type": "jdbc",
      "warehouse": "s3a://warehouse-b",
      "jdbc-url": "jdbc:postgresql://localhost:5432/postgres",
      "jdbc-user": "postgres",
      "jdbc-password": "postgres",
      "catalog-key": "catalog_b"
    }
  }
}
```

## Additional properties

| config                             | default value                  | meaning                                                                        |
|------------------------------------|--------------------------------|--------------------------------------------------------------------------------|
| `type`                             | none                           | Paimon proxy catalog type. Must be `jdbc` for this page.                       |
| `warehouse`                        | none                           | Paimon warehouse path. Required.                                               |
| `jdbc-url`                         | none                           | JDBC endpoint for external Paimon JDBC catalog metadata. Required.             |
| `jdbc-user`                        | none                           | JDBC username.                                                                 |
| `jdbc-password`                    | none                           | JDBC password.                                                                 |
| `jdbc-driver`                      | JDBC driver default resolution | Explicit JDBC driver class name.                                               |
| `jdbc-table-prefix`                | implementation-specific        | Prefix for metadata tables in JDBC backend.                                    |
| `catalog-key`                      | `jdbc`                         | Isolation key in the JDBC metastore. Distinct per catalog when sharing one DB. |
| `fs.s3a.*` (in `fileIoProperties`) | none                           | Hadoop S3A options used by Paimon storage access.                              |
