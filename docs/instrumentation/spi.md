# Instrumentation Capabilities via SPI

Kasanari exposes instrumentation hooks for catalog requests through SPI. Custom listeners can be used for goals such as:

- Audit
- Additional integrations with external services (e.g. OpenMetadata / DataHub)
- Emitting events to trigger updates of related entities

Operational **metrics** (Prometheus) and **tracing** (OTLP) are built into the server. See [Observability](../observability/index.md) for configuration.

## SPI contract

Core interface:

- `kasanari.instrumentation.spi.CatalogRequestListener`

Each listener defines:

- `type()`: unique listener key
- `initialize(CatalogRequestListenerContext)`: startup configuration hook
- Per-engine callbacks for:
  - before request
  - after success
  - on error
  - on access denied

## Runtime loading model

- Listeners are loaded via Java `ServiceLoader`.
- Enabled listeners come from `kasanari.instrumentation.listeners`.
- Listener types are case-insensitive and deduplicated.
- The reserved type `metrics` cannot be registered via SPI (internal use only).

## Built-in listeners

### Audit listener (`audit`)

- Class: `kasanari.instrumentation.audit.AuditCatalogRequestListener`
- Emits operation-level audit records for allowed and denied requests.

### Logging listener (`logging`)

- Class: `kasanari.instrumentation.logging.LoggingCatalogRequestListener`
- Emits before/after/error/denied logs with operation, catalog, and timing details.

### Catalog metrics (always on)

Catalog request counters and timers (`kasanari.catalog.request.*`) are recorded automatically by an internal listener bundled with the server. This listener is **not** controlled by `kasanari.instrumentation.listeners`. See [Metrics](../observability/metrics.md).

## Configuration

Enable multiple listeners:

```properties
kasanari.instrumentation.listeners=audit,logging
```

Enable only one listener:

```properties
kasanari.instrumentation.listeners=logging
```

Disable all SPI listeners:

```properties
kasanari.instrumentation.listeners=
```

## Building a custom listener

1. Implement `CatalogRequestListener`.
2. Provide stable `type()` value (for example `customlistener`). Do not use `metrics`.
3. Register implementation in:
   - `META-INF/services/kasanari.instrumentation.spi.CatalogRequestListener`
4. Put listener artifact on application classpath.
5. Enable it:

```properties
kasanari.instrumentation.listeners=customlistener
```

Per-listener configuration uses the prefix `kasanari.instrumentation.<type>.*` (exposed to `initialize()` without the prefix).

## Classpath wiring

Add the listener JAR to the server runtime classpath:

1. **Gradle dependency** (custom fork/image):

```kotlin
implementation(files("path/to/custom-listener.jar"))
```

2. **Quarkus dev mode**:

```shell
./gradlew :modules:server:quarkusDev \
  -Dquarkus.class-loading.removed=true \
  -Dquarkus.classpath.additions=path/to/custom-listener.jar
```

3. **Container image**: copy the jar into `lib/` or add via your image build.

See [Examples](../examples/index.md) for authentication and authorization SPI patterns.
