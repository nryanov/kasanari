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

| Example path                      | Catalog/mode                                                                | What it demonstrates                                 |
|-----------------------------------|-----------------------------------------------------------------------------|------------------------------------------------------|
| `examples/trino`                  | Iceberg `INTERNAL`                                                          | Trino query engine against Iceberg REST via Kasanari |
| `examples/starrocks`              | Iceberg `INTERNAL`                                                          | StarRocks integration with Iceberg REST via Kasanari |
| `examples/spark`                  | Iceberg `INTERNAL` + Iceberg `PROXY` + Paimon `INTERNAL` + Lance `INTERNAL` | Notebook-driven integration and API registration     |

## Authentication examples

| Example path                     | Provider   | Notes                                 |
|----------------------------------|------------|---------------------------------------|
| `examples/authentication/none`   | `none`     | No authentication required            |
| `examples/authentication/ldap`   | `ldap`     | HTTP Basic against LDAP               |
| `examples/authentication/oidc`   | `oidc`     | JWT Bearer validation via OIDC issuer |
| `examples/authentication/custom` | custom SPI | Header-token auth provider example    |

## Authorization examples

| Example path                    | Provider   | Notes                                    |
|---------------------------------|------------|------------------------------------------|
| `examples/authorization/custom` | custom SPI | Subject allow-list authorization example |
