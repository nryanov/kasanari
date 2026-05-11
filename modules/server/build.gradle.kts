import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.quarkus)
}

dependencies {
    implementation(enforcedPlatform(libs.quarkus.bom))

    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-container-image-jib")
    implementation(libs.casbin.jcasbin)
    implementation(project(":modules:repository:repository-jdbc"))

    // iceberg
    implementation(project(":modules:api:api-management"))
    implementation(project(":modules:api:api-iceberg"))
    implementation(project(":modules:catalog:catalog-iceberg"))
    // paimon
    implementation(project(":modules:api:api-paimon"))
    implementation(project(":modules:catalog:catalog-paimon"))
    // lance
    implementation(project(":modules:api:api-lance"))

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
