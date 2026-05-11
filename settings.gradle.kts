pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id("io.quarkus") version "3.31.2"
        id("org.kordamp.gradle.jandex") version "2.3.0"
    }
}

rootProject.name = "kasanari"

include(
    "modules:server",
    // api
    "modules:api",
    "modules:api:api-iceberg",
    "modules:api:api-paimon",
    "modules:api:api-lance",
    // catalog
    "modules:catalog",
    "modules:catalog:catalog-iceberg",
    "modules:catalog:catalog-paimon",
    "modules:catalog:catalog-lance",
    // platform
    "modules:platform",
    "modules:platform:platform-aws",
    "modules:platform:platform-hadoop",
    "modules:platform:platform-jakarta",
    // repository
    "modules:repository",
    "modules:repository:repository-core",
    "modules:repository:repository-jdbc",
    // fixtures
    "modules:fixtures",
    "modules:fixtures:fixtures-common",
    "modules:fixtures:fixtures-s3",
    "modules:fixtures:fixtures-nessie",
    "modules:fixtures:fixtures-postgres",
    "modules:fixtures:fixtures-hive",
)

findProject(":modules:api")?.name = "api"
findProject(":modules:api:api-iceberg")?.name = "api-iceberg"
findProject(":modules:api:api-paimon")?.name = "api-paimon"
findProject(":modules:api:api-lance")?.name = "api-lance"

findProject(":modules:fixtures")?.name = "fixtures"
findProject(":modules:fixtures:fixtures-common")?.name = "fixtures-common"
findProject(":modules:fixtures:fixtures-s3")?.name = "fixtures-s3"
findProject(":modules:fixtures:fixtures-nessie")?.name = "fixtures-nessie"
findProject(":modules:fixtures:fixtures-postgres")?.name = "fixtures-postgres"
findProject(":modules:fixtures:fixtures-hive")?.name = "fixtures-hive"

findProject(":modules:server")?.name = "server"
findProject(":modules:catalog")?.name = "catalog"
findProject(":modules:catalog:catalog-iceberg")?.name = "catalog-iceberg"
findProject(":modules:catalog:catalog-paimon")?.name = "catalog-paimon"
findProject(":modules:catalog:catalog-lance")?.name = "catalog-lance"

findProject(":modules:platform")?.name = "platform"
findProject(":modules:platform:platform-aws")?.name = "platform-aws"
findProject(":modules:platform:platform-hadoop")?.name = "platform-hadoop"
findProject(":modules:platform:platform-jakarta")?.name = "platform-jakarta"

findProject(":modules:repository")?.name = "repository"
findProject(":modules:repository:repository-core")?.name = "repository-core"
findProject(":modules:repository:repository-jdbc")?.name = "repository-jdbc"
