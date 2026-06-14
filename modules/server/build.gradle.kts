import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.quarkus)
}

dependencies {
    implementation(enforcedPlatform(libs.quarkus.bom))

    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-container-image-jib")
    implementation("io.quarkus:quarkus-security")
    implementation("io.quarkus:quarkus-logging-json")
    implementation("io.quarkus:quarkus-micrometer")
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")
    implementation("io.quarkus:quarkus-opentelemetry")

    // instrumentation
    implementation(project(":modules:instrumentation:instrumentation-listener-runtime"))
    implementation(project(":modules:instrumentation:instrumentation-listener-audit"))
    implementation(project(":modules:instrumentation:instrumentation-listener-logging"))
    implementation(project(":modules:instrumentation:instrumentation-listener-metrics"))

    // auth
    implementation(project(":modules:authentication:authentication-runtime"))
    implementation(project(":modules:authentication:authentication-none"))
    implementation(project(":modules:authentication:authentication-ldap"))
    implementation(project(":modules:authentication:authentication-oidc"))

    // iceberg
    implementation(project(":modules:api:api-iceberg"))
    implementation(project(":modules:catalog:catalog-iceberg"))
    // paimon
    implementation(project(":modules:api:api-paimon"))
    implementation(project(":modules:catalog:catalog-paimon"))
    // lance
    implementation(project(":modules:api:api-lance"))
    implementation(project(":modules:catalog:catalog-lance"))
    // management
    implementation(project(":modules:api:api-management"))
    implementation(project(":modules:management:management-catalog"))
    implementation(project(":modules:repository:repository-jdbc"))

    // authorization
    implementation(project(":modules:authorization:authorization-runtime"))
    implementation(project(":modules:authorization:authorization-allow-all"))
    implementation(project(":modules:authorization:authorization-casbin"))
    implementation(libs.jdbc.postgresql)

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
