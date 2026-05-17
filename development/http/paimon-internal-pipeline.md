# PAIMON INTERNAL — Kasanari JDBC catalog manual test pipeline

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
  "catalogId": "dev-paimon-internal",
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
      "kasanari.catalog.key": "dev-paimon-internal"
    }
  }
}
```

## 1.2 Get catalog metadata (management)

```shell
GET http://localhost:9090/management/v1/catalogs/PAIMON/dev-paimon-internal
Authorization: Bearer dev
```

## 2. Get catalog config (paimon)

```shell
GET http://localhost:9090/paimon/v1/config?warehouse=dev-paimon-internal
Authorization: Bearer dev
```

## 3.1 List databases

```shell
GET http://localhost:9090/paimon/v1/dev-paimon-internal/databases
Authorization: Bearer dev
```

## 3.2 Create database

```shell
POST http://localhost:9090/paimon/v1/dev-paimon-internal/databases
Authorization: Bearer dev
Content-Type: application/json

{
  "name": "demo",
  "options": {}
}
```

## 3.3 List tables in database

```shell
GET http://localhost:9090/paimon/v1/dev-paimon-internal/databases/demo/tables
Authorization: Bearer dev
```

## 3.4 List views in database

```shell
GET http://localhost:9090/paimon/v1/dev-paimon-internal/databases/demo/views
Authorization: Bearer dev
```

## 4.1 Create table

```shell
POST http://localhost:9090/paimon/v1/dev-paimon-internal/databases/demo/tables
Authorization: Bearer dev
Content-Type: application/json

{
  "identifier": {
    "database": "demo",
    "object": "events"
  },
  "schema": {
    "fields": [
      {
        "id": 0,
        "name": "id",
        "type": "INT"
      }
    ],
    "partitionKeys": [],
    "primaryKeys": [],
    "options": {},
    "comment": "test"
  }
}
```

## 4.2 Get table

```shell
GET http://localhost:9090/paimon/v1/dev-paimon-internal/databases/demo/tables/events
Authorization: Bearer dev
```

## 4.3 Alter table

```shell
POST http://localhost:9090/paimon/v1/dev-paimon-internal/databases/demo/tables/events
Authorization: Bearer dev
Content-Type: application/json

{
  "changes": []
}
```

## 4.4 Drop table

```shell
DELETE http://localhost:9090/paimon/v1/dev-paimon-internal/databases/demo/tables/events
Authorization: Bearer dev
```

## 5. Cleanup — delete catalog metadata (optional)

```shell
DELETE http://localhost:9090/management/v1/catalogs/PAIMON/dev-paimon-internal
Authorization: Bearer dev
```
