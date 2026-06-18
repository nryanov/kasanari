# Kasanari (重なり)

**Kasanari** is an open-source REST catalog service for [Apache Iceberg](https://iceberg.apache.org/), [Apache Paimon](https://paimon.apache.org/), and [Lance](https://lancedb.github.io/lance/). 
It provides a unified API gateway so query engines and clients can share catalog metadata across platforms without being tied to a single catalog implementation.

📖 **[Documentation](https://nryanov.github.io/kasanari/)** · [Quickstart](https://nryanov.github.io/kasanari/quickstart/) · [Examples](https://nryanov.github.io/kasanari/examples/)

## Why Kasanari?

Use Kasanari as a full catalog implementation or as a **proxy** in front of an existing catalog:

- **Multi-engine interoperability** — expose Iceberg, Paimon, and Lance catalogs over REST from one service
- **Catalog migration** — move to REST once, then swap the backing implementation without changing client code
- **Engine compatibility** — let engines that only speak REST use JDBC, Hive, Hadoop, or other upstream catalogs

Each catalog runs in one of two modes:

| Mode       | Description                                                                                      |
|------------|--------------------------------------------------------------------------------------------------|
| `INTERNAL` | Kasanari owns metadata lifecycle and storage                                                     |
| `PROXY`    | Kasanari forwards requests to an upstream catalog while keeping a unified API and security layer |

Multiple `INTERNAL` or `PROXY` catalogs of the same type can be registered side by side.

## Supported catalogs

| Catalog        | Version |
|----------------|---------|
| Apache Iceberg | 1.10.1  |
| Apache Paimon  | 1.4.1   |
| Lance          | 0.7.2   |

Proxy backends include JDBC, Hive, Hadoop, filesystem, and REST implementations — see the [catalog docs](https://nryanov.github.io/kasanari/catalogs/iceberg/) for details.

## Features

- **Unified REST APIs** for Iceberg, Paimon, and Lance catalog operations
- **Management API** to register, update, and list catalogs at runtime
- **Pluggable authentication** — `none`, LDAP, OIDC, or custom SPI providers
- **Pluggable authorization** — allow-all, Casbin RBAC, or custom SPI providers
- **Instrumentation SPI** for audit and logging hooks on catalog requests
- **Runnable examples** for Trino, StarRocks, Spark, auth, and observability setups

## Quick start

**Prerequisites:** Docker, Docker Compose, `curl`, and `jq`.

1. Follow the [Quickstart guide](https://nryanov.github.io/kasanari/quickstart/) to bring up PostgreSQL, MinIO, and Kasanari.
2. Register Iceberg, Paimon, and Lance catalogs via the Management API.
3. Point your engine at the REST endpoints (see [Examples](https://nryanov.github.io/kasanari/examples/)).

Or pull a published image from Docker Hub (tags follow release versions, e.g. `v0.1.0`):

```shell
docker run -p 9090:9090 <namespace>/kasanari:v0.1.0
```

## Build from source

**Requirements:** JDK 21, Gradle (wrapper included).

```shell
# Build a local container image (default tag: local/kasanari:<version>)
./scripts/build-container-images.sh

# Cross-platform image build
QUARKUS_JIB_PLATFORMS=linux/amd64 ./scripts/build-container-images.sh
QUARKUS_JIB_PLATFORMS=linux/arm64/v8 ./scripts/build-container-images.sh
```

## Documentation

| Topic                 | Link                                                                                                                                                                                          |
|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Getting started       | [Quickstart](https://nryanov.github.io/kasanari/quickstart/)                                                                                                                                  |
| Catalog configuration | [Iceberg](https://nryanov.github.io/kasanari/catalogs/iceberg/) · [Paimon](https://nryanov.github.io/kasanari/catalogs/paimon/) · [Lance](https://nryanov.github.io/kasanari/catalogs/lance/) |
| Management API        | [API reference](https://nryanov.github.io/kasanari/management/api/)                                                                                                                           |
| Security              | [Authentication & authorization](https://nryanov.github.io/kasanari/security/)                                                                                                                |
| Observability         | [Metrics & tracing](https://nryanov.github.io/kasanari/observability/)                                                                                                                        |
| Examples              | [Runnable examples](https://nryanov.github.io/kasanari/examples/)                                                                                                                             |

## License

Licensed under the [Apache License 2.0](LICENSE).
