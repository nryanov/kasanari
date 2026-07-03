# Authorization example: custom SPI provider

Minimal third-party authorization provider that allows only configured subject names.

## Build

From the repository root:

```shell
./gradlew :examples:authorization:custom:jar
```

The jar is written to `examples/authorization/custom/build/libs/authorization-custom-example.jar`.

## Wire into Kasanari

Add the jar to the server runtime classpath and configure:

```properties
kasanari.authorization.type=allow-list
kasanari.authorization.allow-list.allowed-subjects=alice,bob
```

This provider intentionally demonstrates the minimal SPI contract and only checks `request.subject()`. It ignores resource and permission fields from `AuthorizationRequest`.

## Implement your own provider

1. Implement `kasanari.authorization.spi.AuthorizationProvider` from `authorization-spi`.
2. Register the class in `META-INF/services/kasanari.authorization.spi.AuthorizationProvider`.
3. Optionally implement role binding administration by overriding `roleBindings()` (Casbin provider does this for `/management/v1/security/roles`).
4. Package as a jar, add to the server classpath, set `kasanari.authorization.type=<your-type>`.

Authorization is separate from authentication (`kasanari.authentication.type`).
