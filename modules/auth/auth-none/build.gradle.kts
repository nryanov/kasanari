plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    implementation(project(":modules:auth:auth-spi"))
    implementation("io.smallrye.reactive:mutiny:2.9.4")
}
