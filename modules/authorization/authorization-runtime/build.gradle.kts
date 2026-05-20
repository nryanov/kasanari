plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    api(project(":modules:authorization:authorization-spi"))

    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-core")
    implementation("io.smallrye.config:smallrye-config-core")
    implementation(project(":modules:platform:platform-jakarta"))
}
