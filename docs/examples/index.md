# Examples

This page maps runnable examples from the `examples/` directory.

[//]: # (todo: link to the docker image)
Build Kasanari image once before running containerized examples or download ready-to-use image from docker hub:

```shell
# QUARKUS_JIB_PLATFORMS allows to choose target platform 
QUARKUS_JIB_PLATFORMS=linux/arm64/v8 ./scripts/build-container-images.sh
QUARKUS_JIB_PLATFORMS=linux/amd64 ./scripts/build-container-images.sh
```

## Catalog examples

| Engine / Client | Example path                      | Catalog/mode                                                                | What it demonstrates                                 |
|-----------------|-----------------------------------|-----------------------------------------------------------------------------|------------------------------------------------------|
| Trino           | `examples/trino`                  | Iceberg `INTERNAL`                                                          | Trino query engine against Iceberg REST via Kasanari |
| Starrocks       | `examples/starrocks`              | Iceberg `INTERNAL`                                                          | StarRocks integration with Iceberg REST via Kasanari |
| Spark           | `examples/spark`                  | Iceberg `INTERNAL` + Iceberg `PROXY` + Paimon `INTERNAL` + Lance `INTERNAL` | Notebook-driven integration and API registration     |

## SPI examples

Standalone Gradle project (not part of the main Kasanari build) that demonstrates building custom providers from published Maven artifacts.

| Example path | Coverage | Notes |
|--------------|----------|-------|
| `examples/spi` | authentication SPI | Build auth SPI jar from `com.nryanov.kasanari` Maven deps; see `examples/spi/README.md` |
| `examples/spi/docker` | custom authentication | End-to-end Docker flow with `nryanov/kasanari` image |

## Authentication examples (built-in providers)

| Example path                     | Provider   | Notes                                 |
|----------------------------------|------------|---------------------------------------|
| `examples/authentication/none`   | `none`     | No authentication required            |
| `examples/authentication/ldap`   | `ldap`     | HTTP Basic against LDAP               |
| `examples/authentication/oidc`   | `oidc`     | JWT Bearer validation via OIDC issuer |

## Authorization examples (built-in providers)

Built-in authorization providers ship with the server.

## Observability examples

| Example path                     | Backend       | Notes                                             |
|----------------------------------|---------------|---------------------------------------------------|
| `examples/observability/tracing` | Jaeger (OTLP) | Enable OTLP tracing; metrics remain on Prometheus |
