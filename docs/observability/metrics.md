# Metrics

Kasanari ships with Micrometer and a Prometheus registry. Metrics are scraped from `/q/metrics`.

## Prometheus scrape

| Property                                       | Required | Default      | Description                |
|------------------------------------------------|----------|--------------|----------------------------|
| `quarkus.micrometer.export.prometheus.enabled` | No       | `true`       | Enable Prometheus endpoint |
| `quarkus.micrometer.export.prometheus.path`    | No       | `/q/metrics` | HTTP path for scrapes      |

Example scrape check:

```shell
curl -s http://localhost:9090/q/metrics | head
```

## Platform metrics

These binders are enabled in the default server configuration or available as optional toggles:

| Property                                                | Required | Default | Description                                |
|---------------------------------------------------------|----------|---------|--------------------------------------------|
| `quarkus.micrometer.binder.http-server.enabled`         | No       | `true`  | HTTP server request metrics                |
| `quarkus.micrometer.binder.jvm`                         | No       | `true`  | JVM memory, GC, and thread metrics         |
| `quarkus.micrometer.binder.system`                      | No       | `true`  | CPU and load metrics                       |
| `quarkus.micrometer.export.prometheus.default-registry` | No       | `true`  | Use the default registry for scrape output |

JDBC connection pools created by Kasanari (`KasanariDataSource`) publish Agroal pool metrics when Micrometer is on the classpath.

## Catalog request metrics

The server always records catalog operation metrics (not configurable via `kasanari.instrumentation.listeners`):

| Metric                              | Type    | Tags                                                  |
|-------------------------------------|---------|-------------------------------------------------------|
| `kasanari.catalog.request.total`    | Counter | `engine`, `operation`, `catalog`, `subject`, `outcome` |
| `kasanari.catalog.request.duration` | Timer   | `engine`, `operation`, `catalog`, `subject`, `outcome` |

Tag values:

- `engine` — `iceberg`, `paimon`, or `lance`
- `operation` — catalog REST operation name
- `catalog` — catalog name from the request
- `subject` — authenticated subject from the request (`unknown` when absent)
- `outcome` — `success`, `error`, or `denied`

Denied requests increment the counter only (no duration sample).

## Disabling metrics

Prometheus export can be turned off at runtime:

```properties
quarkus.micrometer.export.prometheus.enabled=false
```

To omit catalog request metrics entirely, remove the `instrumentation-listener-metrics` module from your server build.

## Related

- [Observability overview](index.md)
- [Tracing](tracing.md) — distributed tracing (separate from Micrometer metrics)
