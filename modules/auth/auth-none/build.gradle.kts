plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(libs.smallrye.mutiny)
    implementation(project(":modules:auth:auth-spi"))
}
