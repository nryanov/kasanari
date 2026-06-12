# Paimon `hive` implementation

Support status: **Verified**

- Catalog type: `PAIMON`
- Implementation mode: `PROXY`
- `catalogProperties.type`: `hive`

## INTERNAL catalog setup

Not applicable for this implementation. For Kasanari-owned metadata, use [Paimon Kasanari](kasanari.md).

## PROXY catalog setup

### Minimal runnable config

```json
{
  "catalogId": "paimon_proxy_hive",
  "catalogType": "PAIMON",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "type": "hive",
      "warehouse": "s3a://warehouse",
      "uri": "thrift://localhost:9083"
    }
  }
}
```

### Full config sample

```json
{
  "catalogId": "paimon_proxy_hive",
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
      "type": "hive",
      "warehouse": "s3a://warehouse",
      "uri": "thrift://localhost:9083"
    }
  }
}
```

## Additional properties

| config | default value | meaning |
|---|---|---|
| `type` | none | Paimon proxy catalog type. Must be `hive` for this page. |
| `warehouse` | none | Paimon warehouse path. Required. |
| `uri` | none | Hive Metastore URI used by Paimon Hive catalog. Required. |
| `fs.s3a.*` (in `fileIoProperties`) | none | Hadoop S3A options used by Paimon storage access. |

## Notes

- Kasanari forwards properties to upstream Paimon `CatalogFactory`.
- Feature support may differ from `kasanari` internal mode and depends on Hive-backed Paimon behavior.
