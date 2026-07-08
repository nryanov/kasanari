# Examples

This page maps runnable examples from the `examples/` directory.

Containerized examples use the published [nryanov/kasanari](https://hub.docker.com/r/nryanov/kasanari) image — run `docker compose up -d` in each example directory.

**Contributors:** to build a custom image from source, see [Build from source](https://github.com/nryanov/kasanari#build-from-source) in the repository README (`./scripts/build-container-images.sh`).

## Catalog examples

| Engine / Client | Example path                      | Catalog/mode                                                                | What it demonstrates                                 |
|-----------------|-----------------------------------|-----------------------------------------------------------------------------|------------------------------------------------------|
| Trino           | `examples/trino`                  | Iceberg `INTERNAL`                                                          | Trino query engine against Iceberg REST via Kasanari |
| Starrocks       | `examples/starrocks`              | Iceberg `INTERNAL`                                                          | StarRocks integration with Iceberg REST via Kasanari |
| Spark           | `examples/spark`                  | Iceberg `INTERNAL` + Iceberg `PROXY` + Paimon `INTERNAL` + Lance `INTERNAL` | Notebook-driven integration and API registration     |

## SPI examples

| Example path          | Coverage              |
|-----------------------|-----------------------|
| `examples/spi`        | authentication SPI    |

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
