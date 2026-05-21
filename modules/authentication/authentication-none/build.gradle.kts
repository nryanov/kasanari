plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(libs.smallrye.mutiny)
    implementation(project(":modules:authentication:authentication-spi"))
}
