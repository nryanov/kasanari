# Observability

Kasanari exposes operational telemetry through a fixed observability stack:

- **Metrics** — Micrometer with Prometheus scrape at `/q/metrics`
- **Tracing** — OpenTelemetry with OTLP export (traces only)

Catalog request metrics are recorded automatically when the server includes the metrics instrumentation module. Audit and logging listeners remain configurable through the [instrumentation SPI](instrumentation/spi.md).

## Quick reference

### Metrics (always available in default server)

| Property                                       | Required | Default      | Description                       |
|------------------------------------------------|----------|--------------|-----------------------------------|
| `quarkus.micrometer.export.prometheus.enabled` | No       | `true`       | Expose Prometheus scrape endpoint |
| `quarkus.micrometer.export.prometheus.path`    | No       | `/q/metrics` | Scrape path                       |

See [Metrics](metrics.md) for platform binders, catalog request metric names, and optional tuning.

### Tracing (export disabled until configured)

| Property                              | Required             | Default    | Description                              |
|---------------------------------------|----------------------|------------|------------------------------------------|
| `quarkus.otel.traces.enabled`         | No                   | `true`     | Build-time tracing instrumentation       |
| `quarkus.otel.sdk.disabled`           | No                   | `true`     | Runtime export switch (`false` to export) |
| `quarkus.otel.exporter.otlp.endpoint` | Yes (when exporting) | —          | OTLP gRPC collector URL                  |
| `quarkus.otel.service.name`           | No                   | `kasanari` | Service name in traces                   |

OpenTelemetry **metrics and logs export are disabled** in the default configuration. All numeric metrics use Micrometer/Prometheus.

See [Tracing](tracing.md) for optional sampling and resource attributes.

Catalog metrics are **not** controlled by `kasanari.instrumentation.listeners`. That property applies to ServiceLoader listeners only (`audit`, `logging`, custom SPI).

## Where to go next

- [Metrics](metrics.md) — Prometheus scrape, built-in meters, cardinality guidance
- [Tracing](tracing.md) — OTLP setup and Jaeger example
- [Instrumentation SPI](instrumentation/spi.md) — custom audit/logging integrations
