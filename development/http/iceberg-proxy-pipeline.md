# ICEBERG PROXY — HadoopCatalog on S3 manual test pipeline

## Prerequisites

1. `docker compose -f development/docker-compose.yml up -d`
2. Start Quarkus with dev profile (port 9090)
3. Run requests top-to-bottom (use the IDE run gutter on each `shell` block)

Variables match [`http-client.env.json`](http-client.env.json) `dev` profile.

## 1.1 Register catalog (management)

```shell
POST http://localhost:9090/management/v1/catalogs
Authorization: Bearer dev
Content-Type: application/json

{
  "catalogId": "dev-iceberg-proxy",
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
      "io-impl": "org.apache.iceberg.aws.s3.S3FileIO",
      "s3.endpoint": "http://localhost:9000",
      "s3.access-key-id": "admin",
      "s3.secret-access-key": "password",
      "s3.path-style-access": "true",
      "s3.client-factory": "kasanari.fixtures.s3.NoneRegionS3FileIOAwsClientFactory"
    }
  }
}
```

## 1.2 Get catalog metadata (management)

```shell
GET http://localhost:9090/management/v1/catalogs/ICEBERG/dev-iceberg-proxy
Authorization: Bearer dev
```

## 2. Get catalog config (iceberg)

```shell
GET http://localhost:9090/iceberg/v1/config?warehouse=dev-iceberg-proxy
Authorization: Bearer dev
```

## 3.1 List namespaces (root / flat catalog)

```shell
GET http://localhost:9090/iceberg/v1/dev-iceberg-proxy/namespaces
Authorization: Bearer dev
```

## 3.2 List tables at root namespace (empty namespace segment)

```shell
GET http://localhost:9090/iceberg/v1/dev-iceberg-proxy/namespaces//tables
Authorization: Bearer dev
```

## 4.1 Create table at root namespace

```shell
POST http://localhost:9090/iceberg/v1/dev-iceberg-proxy/namespaces//tables
Authorization: Bearer dev
Content-Type: application/json

{
  "name": "events",
  "location": "s3a://warehouse/events",
  "schema": {
    "type": "struct",
    "fields": [
      {
        "id": 1,
        "name": "id",
        "type": "long",
        "required": true
      }
    ]
  },
  "partition-spec": {
    "spec-id": 0,
    "fields": []
  },
  "write-order": {
    "order-id": 0,
    "fields": []
  },
  "properties": {
    "custom-property": "value"
  }
}
```

## 4.2 Get table (copy metadata.table-uuid for alter)

```shell
GET http://localhost:9090/iceberg/v1/dev-iceberg-proxy/namespaces//tables/events
Authorization: Bearer dev
```

## 4.3 Alter table (replace TABLE_UUID from 4.2 response)

```shell
POST http://localhost:9090/iceberg/v1/dev-iceberg-proxy/namespaces//tables/events
Authorization: Bearer dev
Content-Type: application/json

{
  "requirements": [
    {
      "type": "assert-table-uuid",
      "uuid": "TABLE_UUID"
    }
  ],
  "updates": [
    {
      "action": "set-properties",
      "updates": {
        "custom-property": "updated-value"
      }
    }
  ]
}
```

## 4.4 Drop table

```shell
DELETE http://localhost:9090/iceberg/v1/dev-iceberg-proxy/namespaces//tables/events
Authorization: Bearer dev
```

## 5. Cleanup — delete catalog metadata (optional)

```shell
DELETE http://localhost:9090/management/v1/catalogs/ICEBERG/dev-iceberg-proxy
Authorization: Bearer dev
```
