plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    api(project(":modules:instrumentation:instrumentation-listener-spi"))

    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-core")
    implementation("io.smallrye.config:smallrye-config-core")
    implementation(libs.slf4j)
}
