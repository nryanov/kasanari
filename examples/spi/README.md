# Kasanari authentication SPI example

Standalone Gradle project that shows how to implement a custom authentication provider using [com.nryanov.kasanari](https://mvnrepository.com/artifact/com.nryanov.kasanari) Maven artifacts.

## Build

```shell
cd examples/spi
./gradlew jar
```

Output: `authentication/build/libs/auth-custom-example.jar`

## Run

```shell
cd docker
docker compose up -d
```

## Verify

- Make request without token -> 401

```shell
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:9090/q/metrics
```

- Request with token -> 200

```shell
curl -s -o /dev/null -w "%{http_code}\n" \
  -H "X-Kasanari-Token: dev-secret" \
  http://localhost:9090/q/metrics
```

## Build a new SPI module from scratch

Use this project as a template. The steps below work for authentication, authorization, or instrumentation listeners.

### 1) Create a new Gradle subproject

Add a module directory and register it in `settings.gradle.kts`:

```kotlin
include("my-provider")
```

Create `my-provider/build.gradle.kts` with the SPI dependency you need:

```kotlin
val kasanariVersion: String by project

dependencies {
    // Pick one SPI artifact:
    // implementation("com.nryanov.kasanari:authentication-spi:$kasanariVersion")
    // implementation("com.nryanov.kasanari:authorization-spi:$kasanariVersion")
    // implementation("com.nryanov.kasanari:instrumentation-listener-spi:$kasanariVersion")

    // Authentication providers also need Mutiny at compile time:
    // implementation("io.smallrye.reactive:mutiny:$mutinyVersion")
}

tasks.jar {
    archiveFileName.set("my-provider.jar")
}
```

Published SPI artifacts

| Artifact                                            | Use for                                         |
|-----------------------------------------------------|-------------------------------------------------|
| `com.nryanov.kasanari:authentication-spi`           | Custom auth providers                           |
| `com.nryanov.kasanari:authorization-spi`            | Custom authorization providers                  |
| `com.nryanov.kasanari:instrumentation-listener-spi` | Custom catalog request listeners                |
| `com.nryanov.kasanari:core`                         | Shared types (transitive via authorization-spi) |

Browse versions: [mvnrepository.com/artifact/com.nryanov.kasanari](https://mvnrepository.com/artifact/com.nryanov.kasanari)

### 2) Implement the SPI interface

Create your provider class under `my-provider/src/main/java/...`.

- Authentication: implement `AuthProvider`, return a unique `type()`, read config from `AuthProviderContext` (`kasanari.authentication.<type>.*`).
- Authorization: implement `AuthorizationProvider`, read config from `AuthorizationProviderContext` (`kasanari.authorization.<type>.*`).
- Instrumentation: implement `CatalogRequestListener`, enable via `kasanari.instrumentation.listeners=<type>`.

See the existing modules in `authentication/` and `authorization/` for minimal working implementations.

### 3) Register with Java ServiceLoader

Add a file under `src/main/resources/META-INF/services/`:

```
# Authentication
META-INF/services/kasanari.authentication.spi.AuthProvider
  -> one line per implementation class (FQCN)

# Authorization
META-INF/services/kasanari.authorization.spi.AuthorizationProvider

# Instrumentation
META-INF/services/kasanari.instrumentation.spi.CatalogRequestListener
```

### 4) Build and package

```shell
./gradlew :my-provider:jar
```

### 5) Wire into Kasanari

Put the jar on the server runtime classpath:
- **Container `SPI_EXT_DIR`** (used by `docker/docker-compose.yml`): mount jars into `/opt/kasanari/spi`.
- **Custom image**: copy the jar into the server `lib/` directory.

Configure the provider type:

```properties
kasanari.authentication.type=header-token
kasanari.authentication.header-token.secret=dev-secret
```
