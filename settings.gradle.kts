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
    "modules:api:api-management",
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
    "modules:platform:platform-iceberg",
    "modules:platform:platform-jackson",
    "modules:platform:platform-lance",
    // management
    "modules:management",
    "modules:management:management-catalog",
    // authentication
    "modules:authentication",
    "modules:authentication:authentication-spi",
    "modules:authentication:authentication-runtime",
    "modules:authentication:authentication-none",
    "modules:authentication:authentication-ldap",
    "modules:authentication:authentication-oidc",
    // authorization
    "modules:authorization",
    "modules:authorization:authorization-spi",
    "modules:authorization:authorization-runtime",
    "modules:authorization:authorization-allow-all",
    "modules:authorization:authorization-casbin",
    // repository
    "modules:repository",
    "modules:repository:repository-core",
    "modules:repository:repository-jdbc",
    "modules:repository:repository-iceberg",
    "modules:repository:repository-iceberg:repository-iceberg-postgres",
    "modules:repository:repository-paimon",
    "modules:repository:repository-paimon:repository-paimon-postgres",
    "modules:repository:repository-lance",
    "modules:repository:repository-lance:repository-lance-postgres",
    "modules:repository:repository-management:repository-management-common",
    "modules:repository:repository-management:repository-management-catalog",
    "modules:repository:repository-management:repository-management-catalog:repository-management-catalog-postgres",
    "modules:repository:repository-management:repository-management-security",
    "modules:repository:repository-management:repository-management-security:repository-management-security-postgres",
    // fixtures
    "modules:fixtures",
    "modules:fixtures:fixtures-common",
    "modules:fixtures:fixtures-s3",
    "modules:fixtures:fixtures-nessie",
    "modules:fixtures:fixtures-postgres",
    "modules:fixtures:fixtures-hive",
)

findProject(":modules:api")?.name = "api"
findProject(":modules:api:api-management")?.name = "api-management"
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
findProject(":modules:platform:platform-iceberg")?.name = "platform-iceberg"
findProject(":modules:platform:platform-jackson")?.name = "platform-jackson"
findProject(":modules:platform:platform-lance")?.name = "platform-lance"

findProject(":modules:repository")?.name = "repository"
findProject(":modules:repository:repository-core")?.name = "repository-core"
findProject(":modules:repository:repository-jdbc")?.name = "repository-jdbc"
findProject(":modules:repository:repository-iceberg")?.name = "repository-iceberg"
findProject(":modules:repository:repository-iceberg:repository-iceberg-postgres")?.name = "repository-iceberg-postgres"
findProject(":modules:repository:repository-paimon")?.name = "repository-paimon"
findProject(":modules:repository:repository-paimon:repository-paimon-postgres")?.name = "repository-paimon-postgres"
findProject(":modules:repository:repository-lance")?.name = "repository-lance"
findProject(":modules:repository:repository-lance:repository-lance-postgres")?.name = "repository-lance-postgres"
findProject(":modules:repository:repository-management:repository-management-common")?.name = "repository-management-common"
findProject(":modules:repository:repository-management:repository-management-catalog")?.name = "repository-management-catalog"
findProject(":modules:repository:repository-management:repository-management-catalog:repository-management-catalog-postgres")?.name = "repository-management-catalog-postgres"
findProject(":modules:repository:repository-management:repository-management-security")?.name = "repository-management-security"
findProject(":modules:repository:repository-management:repository-management-security:repository-management-security-postgres")?.name = "repository-management-security-postgres"

findProject(":modules:management")?.name = "management"
findProject(":modules:management:management-catalog")?.name = "management-catalog"
findProject(":modules:authentication")?.name = "authentication"
findProject(":modules:authentication:authentication-spi")?.name = "authentication-spi"
findProject(":modules:authentication:authentication-runtime")?.name = "authentication-runtime"
findProject(":modules:authentication:authentication-none")?.name = "authentication-none"
findProject(":modules:authentication:authentication-ldap")?.name = "authentication-ldap"
findProject(":modules:authentication:authentication-oidc")?.name = "authentication-oidc"

findProject(":modules:authorization")?.name = "authorization"
findProject(":modules:authorization:authorization-spi")?.name = "authorization-spi"
findProject(":modules:authorization:authorization-runtime")?.name = "authorization-runtime"
findProject(":modules:authorization:authorization-allow-all")?.name = "authorization-allow-all"
findProject(":modules:authorization:authorization-casbin")?.name = "authorization-casbin"
