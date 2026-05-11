plugins {
    `java-library`
    id("org.kordamp.gradle.jandex")
}

dependencies {
    api(project(":modules:repository:repository-core"))
    api(libs.jdbi.core)
    api(libs.agroal.pool)
}