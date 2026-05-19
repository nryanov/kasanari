plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    compileOnly(libs.smallrye.mutiny)
}
