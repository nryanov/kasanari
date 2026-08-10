# Quickstart

This quickstart brings up a complete baseline environment and registers an Iceberg, Paimon and Lance catalogs.

Baseline stack:

- PostgreSQL metadata DB
- MinIO object storage
- Kasanari server
- Iceberg, Paimon and Lance catalogs registration in `INTERNAL` mode

## Prerequisites

- Docker + Docker Compose
- `curl` and `jq`

## Setup
### 1) docker-compose.yml
```yaml
services:
  minio:
    image: minio/minio:RELEASE.2025-02-28T09-55-16Z
    environment:
      - MINIO_ROOT_USER=admin
      - MINIO_ROOT_PASSWORD=password
      - MINIO_DOMAIN=minio
    ports:
      - "9000:9000"
      - "9001:9001"
    command: ["server", "/data", "--console-address", ":9001"]

  mc:
    image: minio/minio:RELEASE.2025-02-28T09-55-16Z
    depends_on:
      - minio
    entrypoint: >
      /bin/sh -c "
      until (/usr/bin/mc config host add minio http://minio:9000 admin password) do echo '...waiting...' && sleep 1; done;
      /usr/bin/mc mb minio/warehouse;
      /usr/bin/mc policy set public minio/warehouse;
      tail -f /dev/null
      "

  catalog-storage:
    image: postgres:17
    ports:
      - "5432:5432"
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: postgres

  kasanari:
    image: nryanov/kasanari:latest
    depends_on:
      - catalog-storage
      - minio
      - mc
    ports:
      - "9090:9090"
    environment:
      JVM_OPTS: >-
        -Dkasanari.management.metadata.jdbc-properties."kasanari.jdbc.user"=postgres
        -Dkasanari.management.metadata.jdbc-properties."kasanari.jdbc.password"=postgres
        -Dkasanari.management.metadata.jdbc-properties."uri"=jdbc:postgresql://catalog-storage:5432/postgres
        -Dkasanari.authentication.type=none
      AWS_REGION: none
```

### 2) Download required docker images

This pulls the Kasanari image from [Docker Hub](https://hub.docker.com/r/nryanov/kasanari) along with PostgreSQL, MinIO, and other dependencies.

```shell
docker compose pull
```

### 3) Run services
```shell
docker compose up -d
```

### 4) Register catalogs
In this example INTERNAL catalogs will be used

#### 4.1) Register iceberg catalog
```shell
curl -sS -X POST "http://localhost:9090/management/v1/catalogs" \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": "kasanari_iceberg",
    "catalogType": "ICEBERG",
    "mode": "INTERNAL",
    "spec": {
      "fileIoProperties": {},
      "catalogProperties": {
        "uri": "jdbc:postgresql://catalog-storage:5432/postgres",
        "kasanari.jdbc.user": "postgres",
        "kasanari.jdbc.password": "postgres",
        "warehouse": "s3a://warehouse",
        "io-impl": "org.apache.iceberg.aws.s3.S3FileIO",
        "s3.endpoint": "http://minio:9000",
        "s3.access-key-id": "admin",
        "s3.secret-access-key": "password",
        "s3.path-style-access": "true"
      }
    }
  }' | jq .
```

#### 4.2) Register paimon catalog
```shell
curl -sS -X POST "http://localhost:9090/management/v1/catalogs" \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": kasanari_paimon,
    "catalogType": "PAIMON",
    "mode": "INTERNAL",
    "spec": {
        "fileIoProperties": {},
        "catalogProperties": {
            "warehouse": "s3://warehouse",
            "uri": "jdbc:postgresql://catalog-storage:5432/postgres",
            "kasanari.jdbc.user": "postgres",
            "kasanari.jdbc.password": "postgres",
            "s3.access-key": "admin",
            "s3.secret-key": "password",
            "s3.path.style.access": "true",
            "s3.endpoint": "http://minio:9000",
            "s3.ssl.enabled": "false"
        },
    },
}' | jq .
```

#### 4.3) Register lance catalog
```shell
curl -sS -X POST "http://localhost:9090/management/v1/catalogs" \
  -H "Content-Type: application/json" \
  -d '{
    "catalogId": kasanari_lance,
    "catalogType": "LANCE",
    "mode": "INTERNAL",
    "spec": {
        "fileIoProperties": {},
        "catalogProperties": {
            "implementation": "kasanari.catalog.lance.KasanariLanceCatalog",
            "kasanari.jdbc.user": "postgres",
            "kasanari.jdbc.password": "postgres",
            "uri": "jdbc:postgresql://catalog-storage:5432/postgres",
            "lance.warehouse.location": "s3://warehouse",
            "lance.storage.aws_access_key_id": "admin",
            "lance.storage.aws_secret_access_key": "password",
            "lance.storage.aws_endpoint": "http://minio:9000",
            "lance.storage.aws_allow_http": "true",
            "lance.storage.aws_virtual_hosted_style_request": "false",
            "lance.storage.region": "us-east-1"
        }
    }
}' | jq .
```

### 5) Get catalog metadata
```shell
curl -sS "http://localhost:9090/management/v1/catalogs/iceberg/kasanari_iceberg" | jq .
curl -sS "http://localhost:9090/management/v1/catalogs/paimon/kasanari_paimon" | jq .
curl -sS "http://localhost:9090/management/v1/catalogs/lance/kasanari_lance" | jq .
```
