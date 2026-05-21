plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    api(project(":modules:authentication:authentication-spi"))

    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation("io.quarkus:quarkus-security")
    implementation("io.quarkus:quarkus-vertx-http")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-core")
    implementation("io.smallrye.config:smallrye-config-core")
}
