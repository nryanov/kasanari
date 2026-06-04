# Kasanari

Kasanari is a Java 21 and Quarkus project that exposes REST catalogs for:

- Apache Iceberg
- Apache Paimon
- Lance

For each catalog type, Kasanari supports two operating modes:

- `INTERNAL`: Kasanari provides catalog implementation backed by its own metadata storage.
- `PROXY`: Kasanari routes requests to an upstream catalog implementation.

Kasanari also includes:

- Management API for catalog registration and role binding administration
- Pluggable authentication via SPI (`none`, `ldap`, `oidc`, and custom providers)
- Pluggable authorization via SPI (`allow-all`, `casbin`, and custom providers)
- Request instrumentation listeners via SPI

## Project layout

The repository is a Gradle multi-module build. Main module groups include:

- `modules/server`: Quarkus runtime and HTTP handlers
- `modules/catalog/*`: catalog adapters and internal/proxy factories
- `modules/management/*`: management domain services
- `modules/repository/*`: JDBC repository implementations
- `modules/authentication/*`: authentication SPI and providers
- `modules/authorization/*`: authorization SPI and providers
- `modules/instrumentation/*`: instrumentation SPI and listeners
- `spec/*`: OpenAPI specs used by generated API modules

## Runtime defaults

- HTTP port: `9090`
- Swagger UI: `/docs`
- Health endpoint: `/q/health`
- Catalog refresh interval: `30s`

## How components fit together

1. Clients call catalog or management REST endpoints.
2. Server handlers authorize requests and execute catalog operations.
3. Catalog routers resolve catalog configuration from management metadata.
4. Routers instantiate `INTERNAL` or `PROXY` adapters and refresh them periodically.
5. Instrumentation listeners observe allowed, denied, and failed operations.
