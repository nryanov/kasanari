# Catalog examples

All examples are self-contained and runnable with `docker compose` from their own directory.

Build the server image once from the repository root:

```shell
./scripts/build-container-images.sh
```

## Catalog families

- [Iceberg + Trino (internal)](trino/README.md)
- [Iceberg + StarRocks (internal)](starrocks/README.md)
- [Spark notebooks (Iceberg/Paimon/Lance)](spark/README.md)
- [Lance examples](lance/README.md)

## Existing auth-focused examples

- [Auth none](authentication/none/README.md)
- [Auth LDAP](authentication/ldap/README.md)
- [Auth OIDC](authentication/oidc/README.md)
- [Auth custom provider](authentication/custom/README.md)
- [Authorization custom provider](authorization/custom/README.md)
