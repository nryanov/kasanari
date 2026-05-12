plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    api(enforcedPlatform(libs.jackson.bom))

    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("com.fasterxml.jackson.core:jackson-core")
}