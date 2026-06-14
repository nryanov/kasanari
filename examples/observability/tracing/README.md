# Tracing via OTLP (Jaeger)

Distributed tracing with OpenTelemetry OTLP export to [Jaeger](https://www.jaegertracing.io/).

Metrics stay on Micrometer/Prometheus (`/q/metrics`). This example configures **traces only**.

## Prerequisites

- Docker and Docker Compose plugin
- `curl`

Build the local Kasanari image from repository root (or set `KASANARI_IMAGE` to your own tag):

```shell
./scripts/build-container-images.sh
```

## Start services

Starts MinIO (S3), PostgreSQL (management metadata), Jaeger (OTLP collector + UI), and Kasanari:

```shell
cd examples/observability/tracing
docker compose up -d
```

Wait a few seconds for MinIO bucket bootstrap (`mc` service), PostgreSQL, and Kasanari startup.

| Service            | Endpoint                                                           | Credentials                 |
|--------------------|--------------------------------------------------------------------|-----------------------------|
| Kasanari           | [http://localhost:9090](http://localhost:9090)                     | auth: `none`                |
| PostgreSQL         | `localhost:5432`                                                   | user / password: `postgres` |
| MinIO S3 API       | `http://localhost:9000`                                            | `admin` / `password`        |
| MinIO console      | `http://localhost:9001`                                            | `admin` / `password`        |
| Jaeger UI          | [http://localhost:16686](http://localhost:16686)                   | —                           |
| OTLP gRPC          | `http://localhost:4317`                                            | —                           |
| Prometheus metrics | [http://localhost:9090/q/metrics](http://localhost:9090/q/metrics) | —                           |

Default bucket: `warehouse` (public read). Override the image tag if needed:

```shell
KASANARI_IMAGE=local/kasanari:latest docker compose up -d
```

## Generate traces

```shell
curl -s http://localhost:9090/q/health
curl -s http://localhost:9090/q/metrics -o /dev/null -w "%{http_code}\n"
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:9090/management/v1/catalogs
```

## View traces

1. Open [http://localhost:16686](http://localhost:16686)
2. Select service **kasanari**
3. Click **Find Traces**

You should see spans for HTTP requests handled by the server.

## Stop services

```shell
cd examples/observability/tracing
docker compose down
```

## Related documentation

- [Tracing](../../docs/observability/tracing.md)
- [Observability overview](../../docs/observability/index.md)
