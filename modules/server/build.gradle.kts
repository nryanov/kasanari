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

    // auth
    implementation(project(":modules:auth:auth-runtime"))
    implementation(project(":modules:auth:auth-none"))
    implementation(project(":modules:auth:auth-ldap"))
    implementation(project(":modules:auth:auth-oidc"))

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
    implementation(project(":modules:management:management-security"))
    implementation(project(":modules:repository:repository-jdbc"))

    implementation(libs.casbin.jcasbin)
    implementation(libs.jdbc.postgresql)

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
