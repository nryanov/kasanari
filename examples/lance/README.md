# Lance examples

This directory contains runnable Lance/LanceDB-focused Kasanari examples.

## Examples

- [`lancedb/internal`](./lancedb/internal): LANCE `INTERNAL` mode with a LanceDB Python client container.
- [`lancedb/proxy`](./lancedb/proxy): LANCE `PROXY` mode with a LanceDB Python client container.
- [`secure-oidc-rbac`](./secure-oidc-rbac): LANCE secure setup with OIDC auth and Casbin RBAC authorization.

## Build Kasanari image once

From repository root:

```shell
./scripts/build-container-images.sh
```
