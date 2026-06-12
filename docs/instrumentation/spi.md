# Instrumentation Capabilities via SPI

Kasanari exposes instrumentation hooks for catalog requests through SPI. Implementing custom listener multiple goals can be achieved, for example:
- Custom metrics
- Audit
- Additional integrations with other external services (e.g. OpenMetadata / DataHub)
- Emitting events -> trigger updates of related entities

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

## Built-in listeners

### Audit listener (`audit`)

- Class: `kasanari.instrumentation.audit.AuditCatalogRequestListener`
- Emits operation-level audit records for allowed and denied requests.

### Logging listener (`logging`)

- Class: `kasanari.instrumentation.logging.LoggingCatalogRequestListener`
- Emits before/after/error/denied logs with operation, catalog, and timing details.

## Configuration

Enable multiple listeners:

```properties
kasanari.instrumentation.listeners=audit,logging
```

Enable only one listener:

```properties
kasanari.instrumentation.listeners=logging
```

Disable all listeners:

```properties
kasanari.instrumentation.listeners=
```

## Building a custom listener

1. Implement `CatalogRequestListener`.
2. Provide stable `type()` value (for example `customlistener`).
3. Register implementation in:
   - `META-INF/services/kasanari.instrumentation.spi.CatalogRequestListener`
4. Put listener artifact on application classpath.
5. Enable it:

```properties
kasanari.instrumentation.listeners=customlistener
```
