plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(project(":modules:instrumentation:instrumentation-listener-spi"))
    implementation(libs.slf4j)
}
