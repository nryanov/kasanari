# Paimon `filesystem` implementation

Support status: **Verified**

- Catalog type: `PAIMON`
- Implementation mode: `PROXY`
- `catalogProperties.type`: `filesystem`

## INTERNAL catalog setup

Not applicable for this implementation. For Kasanari-owned metadata, use [Paimon Kasanari](kasanari.md).

## PROXY catalog setup

### Minimal runnable config

```json
{
  "catalogId": "paimon_proxy_filesystem",
  "catalogType": "PAIMON",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "type": "filesystem",
      "warehouse": "s3a://warehouse"
    }
  }
}
```

### Full config sample (`s3a://` with Hadoop config)

```json
{
  "catalogId": "paimon_proxy_filesystem",
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

### Full config sample (`s3://` with catalog options)

```json
{
  "catalogId": "paimon_proxy_filesystem_s3",
  "catalogType": "PAIMON",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "type": "filesystem",
      "warehouse": "s3://warehouse",
      "s3.access-key": "admin",
      "s3.secret-key": "password",
      "s3.endpoint": "http://localhost:9000",
      "s3.path.style.access": "true"
    }
  }
}
```

## Additional properties

| config | default value | meaning |
|---|---|---|
| `type` | none | Paimon proxy catalog type. Must be `filesystem` for this page. |
| `warehouse` | none | Filesystem warehouse root path. Required. |
| `fs.s3a.*` (in `fileIoProperties`) | none | Hadoop S3A options, commonly used with `s3a://` warehouse URIs. |
| `s3.access-key` | none | Paimon S3 access key (used when configured via catalog properties). |
| `s3.secret-key` | none | Paimon S3 secret key. |
| `s3.endpoint` | none | S3 endpoint URL (for example MinIO). |
| `s3.path.style.access` | none | Enables path-style addressing for S3-compatible storage. |

## Notes

- Both `s3a://` and `s3://` styles are used in tested setups.
- Pick one approach (Hadoop `fileIoProperties` or Paimon `s3.*` options) consistent with your runtime.
