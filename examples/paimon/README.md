# Paimon examples

This directory contains runnable Paimon-focused Kasanari examples.

## Examples

- [`spark/internal`](./spark/internal): PAIMON `INTERNAL` mode with Spark notebook client.
- [`spark/proxy-jdbc`](./spark/proxy-jdbc): PAIMON `PROXY` mode with JDBC-oriented proxy registration (best effort) and Spark notebook client.
- [`flink/internal`](./flink/internal): PAIMON `INTERNAL` mode with Flink client and notebook.
- [`flink/proxy-jdbc`](./flink/proxy-jdbc): PAIMON `PROXY` mode with JDBC-oriented proxy registration (best effort) and Flink client/notebook.
- [`secure-oidc-rbac`](./secure-oidc-rbac): PAIMON secure setup with OIDC auth and Casbin RBAC authorization.

## Build Kasanari image once

From repository root:

```shell
./scripts/build-container-images.sh
```
