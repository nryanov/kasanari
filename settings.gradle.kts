pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
    }
}

rootProject.name = "kasanari"

include(
    "modules:api",
    "modules:api:api-iceberg",
    "modules:api:api-paimon",
    "modules:server",
    "modules:catalog",
    "modules:catalog:catalog-iceberg-core",
    "modules:catalog:catalog-iceberg-common",
    "modules:catalog:catalog-iceberg-hadoop",
    "modules:catalog:catalog-iceberg-inmemory",
    "modules:catalog:catalog-iceberg-hive",
    "modules:catalog:catalog-iceberg-nessie",
    "modules:catalog:catalog-iceberg-rest",
    "modules:catalog:catalog-iceberg-kasanari",
    "modules:catalog:catalog-iceberg-jdbc",
    "modules:catalog:catalog-paimon-core",
    "modules:catalog:catalog-paimon-kasanari",
    "modules:catalog:catalog-paimon-jdbc",
    "modules:catalog:catalog-paimon-hive",
    "modules:catalog:catalog-paimon-rest",
    "modules:catalog:catalog-paimon-filesystem",
    "modules:fixtures",
    "modules:fixtures:fixtures-s3",
    "modules:fixtures:fixtures-nessie",
    "modules:fixtures:fixtures-postgres",
    "modules:fixtures:fixtures-hive",
)

findProject(":modules:api")?.name = "api"
findProject(":modules:api:api-iceberg")?.name = "api-iceberg"
findProject(":modules:api:api-paimon")?.name = "api-paimon"

findProject(":modules:fixtures")?.name = "fixtures"
findProject(":modules:fixtures:fixtures-s3")?.name = "fixtures-s3"
findProject(":modules:fixtures:fixtures-nessie")?.name = "fixtures-nessie"
findProject(":modules:fixtures:fixtures-postgres")?.name = "fixtures-postgres"
findProject(":modules:fixtures:fixtures-hive")?.name = "fixtures-hive"

findProject(":modules:server")?.name = "server"
findProject(":modules:catalog")?.name = "catalog"
findProject(":modules:catalog:catalog-iceberg-core")?.name = "catalog-iceberg-core"
findProject(":modules:catalog:catalog-iceberg-common")?.name = "catalog-iceberg-common"
findProject(":modules:catalog:catalog-iceberg-hadoop")?.name = "catalog-iceberg-hadoop"
findProject(":modules:catalog:catalog-iceberg-inmemory")?.name = "catalog-iceberg-inmemory"
findProject(":modules:catalog:catalog-iceberg-hive")?.name = "catalog-iceberg-hive"
findProject(":modules:catalog:catalog-iceberg-nessie")?.name = "catalog-iceberg-nessie"
findProject(":modules:catalog:catalog-iceberg-rest")?.name = "catalog-iceberg-rest"
findProject(":modules:catalog:catalog-iceberg-kasanari")?.name = "catalog-iceberg-kasanari"
findProject(":modules:catalog:catalog-iceberg-jdbc")?.name = "catalog-iceberg-jdbc"
findProject(":modules:catalog:catalog-paimon-kasanari")?.name = "catalog-paimon-kasanari"
findProject(":modules:catalog:catalog-paimon-jdbc")?.name = "catalog-paimon-jdbc"
findProject(":modules:catalog:catalog-paimon-hive")?.name = "catalog-paimon-hive"
findProject(":modules:catalog:catalog-paimon-rest")?.name = "catalog-paimon-rest"
findProject(":modules:catalog:catalog-paimon-filesystem")?.name = "catalog-paimon-filesystem"
findProject(":modules:catalog:catalog-paimon-core")?.name = "catalog-paimon-core"
