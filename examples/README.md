# Catalog examples

All examples are self-contained and runnable with `docker compose` from their own directory.

Build the server image once from the repository root:

```shell
./scripts/build-container-images.sh
```

## Catalog families

- [Iceberg examples](iceberg/README.md)
- [Paimon examples](paimon/README.md)
- [Lance examples](lance/README.md)

## Existing auth-focused examples

- [Auth none](auth-none/README.md)
- [Auth LDAP](auth-ldap/README.md)
- [Auth OIDC](auth-oidc/README.md)
- [Auth custom provider](auth-custom/README.md)
- [Authorization custom provider](authorization-custom/README.md)
