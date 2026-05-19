plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(enforcedPlatform(libs.quarkus.bom))
}