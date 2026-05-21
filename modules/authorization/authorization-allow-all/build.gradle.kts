plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(project(":modules:authorization:authorization-spi"))
}
