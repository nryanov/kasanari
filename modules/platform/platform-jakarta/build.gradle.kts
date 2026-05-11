plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    api(libs.jakarta.annotation.api)
    api(libs.jakarta.inject.api)
    api(libs.jakarta.ws.rs.api)
}