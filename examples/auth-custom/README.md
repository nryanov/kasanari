# Auth example: custom SPI provider

Minimal third-party auth provider that validates a shared secret sent in the `X-Kasanari-Token` header.

This example is **not wired into the server by default**. Build the jar and add it to the server classpath, or depend on it from a custom server image.

## Build

From the repository root:

```shell
./gradlew :examples:auth-custom:jar
```

The jar is written to `examples/auth-custom/build/libs/auth-custom-example.jar`.

## Wire into Kasanari

Add the jar to the server runtime classpath. Options:

1. **Gradle dependency** (custom fork/image):

```kotlin
implementation(files("examples/auth-custom/build/libs/auth-custom-example.jar"))
// or publish the example module and depend on it normally
```

2. **Quarkus dev mode**:

```shell
./gradlew :modules:server:quarkusDev \
  -Dquarkus.class-loading.removed=true \
  -Dquarkus.classpath.additions=examples/auth-custom/build/libs/auth-custom-example.jar
```

3. **Container image**: copy the jar into `lib/` or add via your image build.

## Configure

```shell
export KASANARI_AUTH_TYPE=header-token
export KASANARI_AUTH_HEADER_TOKEN_SECRET=dev-secret
export KASANARI_AUTH_HEADER_TOKEN_HEADER=X-Kasanari-Token

./gradlew :modules:server:quarkusDev
```

Properties map to `kasanari.auth.header-token.*` because the provider type is `header-token`.

## Verify

Without token (expect 401):

```shell
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:9090/management/v1/catalogs
```

With token (expect 200 or 404 depending on data):

```shell
curl -s -o /dev/null -w "%{http_code}\n" \
  -H "X-Kasanari-Token: dev-secret" \
  http://localhost:9090/management/v1/catalogs
```

Health and Swagger UI stay public: `/q/health`, `/docs`.

## Implement your own provider

1. Implement `kasanari.auth.spi.AuthProvider` from `auth-spi`.
2. Register the class in `META-INF/services/kasanari.auth.spi.AuthProvider`.
3. Return a unique `type()` string and read config from `AuthProviderContext` (`kasanari.auth.<type>.*`).
4. Package as a jar and add to the server classpath.
5. Set `kasanari.auth.type=<your-type>` and restart.

See `src/main/java/kasanari/auth/custom/HeaderTokenAuthProvider.java` in this directory.
