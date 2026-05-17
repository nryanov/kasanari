# LANCE PROXY — dir implementation on S3 manual test pipeline

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
  "catalogId": "dev-lance-proxy",
  "catalogType": "LANCE",
  "mode": "PROXY",
  "spec": {
    "fileIoProperties": {},
    "catalogProperties": {
      "implementation": "dir",
      "root": "s3://warehouse",
      "manifest_enabled": "false",
      "dir_listing_enabled": "true",
      "storage.aws_access_key_id": "admin",
      "storage.aws_secret_access_key": "password",
      "storage.aws_endpoint": "http://localhost:9000",
      "storage.aws_allow_http": "true",
      "storage.aws_virtual_hosted_style_request": "false",
      "storage.access_key_id": "admin",
      "storage.secret_access_key": "password",
      "storage.endpoint": "http://localhost:9000",
      "storage.allow_http": "true",
      "storage.virtual_hosted_style_request": "false",
      "storage.region": "us-east-1"
    }
  }
}
```

## 1.2 Get catalog metadata (management)

```shell
GET http://localhost:9090/management/v1/catalogs/LANCE/dev-lance-proxy
Authorization: Bearer dev
```

## 3.1 List namespaces at root (id = ".")

```shell
GET http://localhost:9090/lance/v1/namespace/./list
Authorization: Bearer dev
```

## 3.2 List tables at root namespace

```shell
GET http://localhost:9090/lance/v1/namespace/./table/list
Authorization: Bearer dev
```

## 4.1 Register table (JSON; no Arrow IPC create)

```shell
POST http://localhost:9090/lance/v1/table/events/register
Authorization: Bearer dev
Content-Type: application/json

{
  "id": ["events"],
  "location": "s3://warehouse/events.lance",
  "mode": "CREATE",
  "properties": {
    "stage": "manual-test"
  }
}
```

## 4.2 Describe table (get)

```shell
POST http://localhost:9090/lance/v1/table/events/describe
Authorization: Bearer dev
Content-Type: application/json

{
  "id": ["events"]
}
```

## 4.3 Alter table columns

```shell
POST http://localhost:9090/lance/v1/table/events/alter_columns
Authorization: Bearer dev
Content-Type: application/json

{
  "id": ["events"],
  "alterations": [
    {
      "column": "id",
      "rename": "id"
    }
  ]
}
```

## 4.4 Drop table

```shell
POST http://localhost:9090/lance/v1/table/events/drop
Authorization: Bearer dev
Content-Type: application/json

{
  "id": ["events"]
}
```

## 5. Cleanup — delete catalog metadata (optional)

```shell
DELETE http://localhost:9090/management/v1/catalogs/LANCE/dev-lance-proxy
Authorization: Bearer dev
```
