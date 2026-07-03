# Tracing

Kasanari uses the Quarkus OpenTelemetry extension for **distributed tracing only**. Metrics remain on Micrometer/Prometheus; OpenTelemetry metrics and logs export are disabled in the default configuration.

## Enable tracing

Tracing instrumentation is enabled in the default server build. Export stays off until you configure an OTLP endpoint and enable the SDK at runtime:

```properties
quarkus.otel.sdk.disabled=false
quarkus.otel.exporter.otlp.endpoint=http://localhost:4317
quarkus.otel.service.name=kasanari
```

| Property                              | Required                   | Default    | Description                                           |
|---------------------------------------|----------------------------|------------|-------------------------------------------------------|
| `quarkus.otel.traces.enabled`         | No                         | `true`     | Build-time switch for tracing instrumentation         |
| `quarkus.otel.sdk.disabled`           | No                         | `true`     | Runtime switch; set to `false` to export traces         |
| `quarkus.otel.exporter.otlp.endpoint` | Yes (when exporting)       | —          | OTLP gRPC endpoint (host:port)                        |
| `quarkus.otel.service.name`           | No                         | `kasanari` | Logical service name in trace UI                      |

`quarkus.otel.traces.enabled` is fixed at build time. Use `quarkus.otel.sdk.disabled=false` plus an OTLP endpoint to turn export on at runtime without rebuilding.

### Disabled by default (keep metrics on Micrometer)

| Property                       | Default | Description                |
|--------------------------------|---------|----------------------------|
| `quarkus.otel.metrics.enabled` | `false` | Do not export OTel metrics |
| `quarkus.otel.logs.enabled`    | `false` | Do not export OTel logs    |

## Optional configuration

| Property                             | Description                                                           |
|--------------------------------------|-----------------------------------------------------------------------|
| `quarkus.otel.exporter.otlp.headers` | Comma-separated `key=value` headers for authenticated collectors      |
| `quarkus.otel.resource.attributes`   | Extra resource attributes (for example `deployment.environment=prod`) |
| `quarkus.otel.traces.sampler`        | Sampling strategy (for example `traceidratio`)                        |
| `quarkus.otel.traces.sampler.arg`    | Sampler argument (for example `0.1` for 10% sampling)                 |

Environment variable equivalents use the `QUARKUS_OTEL_*` prefix (for example `QUARKUS_OTEL_SDK_DISABLED=false`).

## Runnable example: Jaeger

See `examples/observability/tracing/README.md` in the repository:

1. Start Jaeger with OTLP enabled via Docker Compose
2. Enable tracing properties on Kasanari
3. Send HTTP requests and inspect traces in the Jaeger UI at `http://localhost:16686`

## Related

- [Observability overview](index.md)
- [Metrics](metrics.md) — Prometheus scrape and catalog request meters
