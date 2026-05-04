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
    "modules:api",
    "modules:api:api-iceberg",
    "modules:api:api-paimon",
    "modules:api:api-lance",
    "modules:server",
    "modules:catalog",
    "modules:catalog:catalog-iceberg",
    "modules:catalog:catalog-paimon",
    "modules:catalog:catalog-lance",
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
