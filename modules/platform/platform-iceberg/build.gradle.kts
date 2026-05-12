plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    api(enforcedPlatform(libs.iceberg.platform)) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
}
